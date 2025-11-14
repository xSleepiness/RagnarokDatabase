# Arquitectura - Ragnarok Database

## Diagrama de Flujo de Datos

```
┌─────────────────────────────────────────────────────────────┐
│                         UI Layer                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  MainActivity (ComponentActivity)                     │  │
│  │  └── MainScreen (Composable)                         │  │
│  │       ├── PeriodFilterChips                          │  │
│  │       ├── PopularItemCard                            │  │
│  │       └── ErrorCard                                  │  │
│  └──────────────────────────────────────────────────────┘  │
│                          ▲                                  │
│                          │ observes StateFlow               │
│                          │ collectAsState()                 │
└──────────────────────────┼──────────────────────────────────┘
                           │
┌──────────────────────────┼──────────────────────────────────┐
│                   ViewModel Layer                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  MainViewModel (ViewModel)                           │  │
│  │  ├── uiState: StateFlow<MainUiState>                │  │
│  │  ├── selectedPeriod: StateFlow<String>              │  │
│  │  └── loadPopularItems(period: String)               │  │
│  │      └── uses viewModelScope + try/catch             │  │
│  └──────────────────────────────────────────────────────┘  │
│                          ▲                                  │
│                          │ creates repository instance      │
│                          │                                  │
└──────────────────────────┼──────────────────────────────────┘
                           │
┌──────────────────────────┼──────────────────────────────────┐
│                   Repository Layer                          │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  ItemRepository                                      │  │
│  │  ├── getItem(id): Item                               │  │
│  │  └── getPopularItems(period, limit): List<PopularItem>│ │
│  │                                                       │  │
│  │  MonsterRepository                                   │  │
│  │  └── getMonster(id): Monster                         │  │
│  └──────────────────────────────────────────────────────┘  │
│                          ▲                                  │
│                          │ uses                             │
│                          │                                  │
└──────────────────────────┼──────────────────────────────────┘
                           │
┌──────────────────────────┼──────────────────────────────────┐
│                   Data Source Layer                         │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  RagnarokApiService (Retrofit Interface)             │  │
│  │  ├── getItem(@Path id): Item                         │  │
│  │  ├── getMonster(@Path id): Monster                   │  │
│  │  └── getPopularItems(@Path period, @Query limit):    │  │
│  │      PopularItemsResponse                            │  │
│  └──────────────────────────────────────────────────────┘  │
│                          ▲                                  │
│                          │ created by                       │
│                          │                                  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  NetworkModule (object singleton)                    │  │
│  │  └── api: RagnarokApiService (lazy)                 │  │
│  │       ├── Retrofit.Builder()                         │  │
│  │       ├── baseUrl: "http://10.0.2.2:8000/api/v1/"   │  │
│  │       └── GsonConverterFactory (JSON parsing)        │  │
│  └──────────────────────────────────────────────────────┘  │
└──────────────────────────┼──────────────────────────────────┘
                           │
                           │ HTTP Requests (suspend functions)
                           ▼
                  ┌─────────────────┐
                  │   FastAPI Server│
                  │  10.0.2.2:8000  │
                  │ (127.0.0.1:8000)│
                  └─────────────────┘
```

## Componentes Principales

### 1. **UI Layer** (view/)
- **MainScreen.kt**: Pantalla principal con lista de items populares
  - `MainScreen()`: Composable principal que observa el ViewModel
  - `PeriodFilterChips()`: Filtros por período (today, yesterday, last7days, last30days)
  - `PopularItemCard()`: Card individual para cada item con imagen y nombre
  - `ErrorCard()`: Muestra errores de red
- Usa `collectAsState()` para observar StateFlow
- No contiene lógica de negocio
- Manejo de estados: Loading, Success, Error

### 2. **ViewModel Layer** (viewmodel/)
- **MainViewModel.kt**: Maneja el estado de la pantalla principal
  - `_uiState: MutableStateFlow<MainUiState>` (privado)
  - `uiState: StateFlow<MainUiState>` (público, solo lectura)
  - `selectedPeriod: StateFlow<String>` (período actual)
  - `loadPopularItems(period: String)`: Carga items por período
  - Usa `viewModelScope.launch` para corrutinas
  - Try/catch para manejo de errores
- **MainUiState**: Sealed class con 3 estados
  - `Loading`: Cargando datos
  - `Success(items: List<PopularItem>)`: Datos cargados
  - `Error(message: String)`: Error en la carga

### 3. **Repository Layer** (data/repository/)
- **ItemRepository.kt**: Abstracción de datos de items
  - `getItem(itemId: Int): Item` - Obtiene detalle de un item
  - `getPopularItems(period: String, limit: Int): List<PopularItem>` - Items populares
- **MonsterRepository.kt**: Abstracción de datos de monstruos
  - `getMonster(monsterId: Int): Monster` - Obtiene detalle de un monstruo
- Todas las funciones son `suspend` para uso con corrutinas
- Recibe `RagnarokApiService` por parámetro (inyección manual por defecto)

### 4. **Data Source Layer** (data/remote/)
- **NetworkModule.kt**: Configuración de Retrofit (object singleton)
  - `api: RagnarokApiService` creado de forma lazy
  - `BASE_URL = "http://10.0.2.2:8000/api/v1/"` (para emulador Android)
  - Usa `GsonConverterFactory` para parsear JSON
- **RagnarokApiService.kt**: Define endpoints de Retrofit
  - Todos los métodos son `suspend fun` para corrutinas
  - Usa anotaciones `@GET`, `@Path`, `@Query`

### 5. **Model Layer** (model/)
- **Item.kt**: Modelos de items del juego
  - `Item`: Modelo completo con stats, precio, nivel requerido, etc.
  - `ItemStats`: Stats del item (atk, matk, defense, weight, slots)
  - `PopularItem`: Modelo simplificado para items populares (id, name, type, viewCount)
  - `PopularItemsResponse`: Wrapper de respuesta del backend (period, items)
- **Monster.kt**: Modelo de monstruos
- Todos usan `@SerializedName` para mapear campos JSON → Kotlin

### 6. **Theme Layer** (ui/theme/)
- **Color.kt**: Colores de Tailwind CSS adaptados
- **Theme.kt**: Tema dark de la app
- **Type.kt**: Tipografía usando Lilita One para headers

## Flujo de Datos

```
User Action (Period Filter) 
    ↓
MainScreen (Composable)
    ↓
viewModel.loadPopularItems(period)
    ↓
MainViewModel.viewModelScope.launch
    ↓
ItemRepository.getPopularItems(period, limit)
    ↓
RagnarokApiService.getPopularItems(period, limit)
    ↓
Retrofit HTTP Request → FastAPI Backend
    ↓
JSON Response → Gson → PopularItemsResponse
    ↓
Repository extrae .items → List<PopularItem>
    ↓
ViewModel actualiza _uiState.value = Success(items)
    ↓
StateFlow emite nuevo estado
    ↓
MainScreen recompone con collectAsState()
    ↓
UI muestra items en LazyColumn
```

## Manejo de Estados

Tu app usa **Sealed Classes** para estados tipados:

```kotlin
sealed class MainUiState {
    object Loading : MainUiState()
    data class Success(val items: List<PopularItem>) : MainUiState()
    data class Error(val message: String) : MainUiState()
}
```

**Uso en ViewModel:**
```kotlin
fun loadPopularItems(period: String) {
    viewModelScope.launch {
        _uiState.value = MainUiState.Loading
        try {
            val items = itemRepository.getPopularItems(period, limit = 10)
            _uiState.value = MainUiState.Success(items)
        } catch (e: Exception) {
            _uiState.value = MainUiState.Error(e.message ?: "Error desconocido")
        }
    }
}
```

**Uso en UI:**
```kotlin
val uiState by viewModel.uiState.collectAsState()

when (uiState) {
    is MainUiState.Loading -> CircularProgressIndicator()
    is MainUiState.Success -> LazyColumn { items((uiState as MainUiState.Success).items) }
    is MainUiState.Error -> ErrorCard(message = (uiState as MainUiState.Error).message)
}
```

## Gestión de Dependencias

Actualmente tu proyecto usa **inyección manual de dependencias**:

```kotlin
// ViewModel crea su propio repository
class MainViewModel(
    private val itemRepository: ItemRepository = ItemRepository()
) : ViewModel()

// Repository crea su propio API service
class ItemRepository(
    private val api: RagnarokApiService = NetworkModule.api
)

// NetworkModule es un singleton object
object NetworkModule {
    val api: RagnarokApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RagnarokApiService::class.java)
    }
}
```

**Ventajas:** Simple, sin dependencias externas  
**Desventajas:** Dificulta testing, acoplamiento fuerte

### Mejora futura: Usar Hilt o Koin
Para producción se recomienda usar un framework de DI como Hilt o Koin.

## Thread Management

Tu app usa **Kotlin Coroutines** para operaciones asíncronas:

- **Main Thread (UI)**: 
  - Composables y recomposiciones
  - `collectAsState()` observa StateFlow
  - Actualizaciones de UI

- **Background Thread (IO)**:
  - Retrofit automáticamente ejecuta `suspend fun` en background
  - ViewModelScope maneja lifecycle automáticamente
  - No necesitas `.flowOn(Dispatchers.IO)` con Retrofit

**Ejemplo:**
```kotlin
// En ViewModel - se ejecuta en Main pero Retrofit usa IO internamente
viewModelScope.launch {
    _uiState.value = MainUiState.Loading  // Main thread
    try {
        val items = repository.getPopularItems(period, limit)  // Retrofit → IO thread
        _uiState.value = MainUiState.Success(items)  // Main thread
    } catch (e: Exception) {
        _uiState.value = MainUiState.Error(e.message ?: "Error")  // Main thread
    }
}
```

## Configuración de Red

### Emulador Android
```kotlin
private const val BASE_URL = "http://10.0.2.2:8000/api/v1/"
```
- `10.0.2.2` es la IP especial para acceder al localhost del host desde el emulador
- Equivale a `127.0.0.1:8000` en tu PC

### Dispositivo Físico
Para probar en dispositivo real, cambia a la IP de tu PC en la red local:
```kotlin
private const val BASE_URL = "http://192.168.1.42:8000/api/v1/"
```

### AndroidManifest.xml
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<application
    android:usesCleartextTraffic="true"
    ...>
```

## Tecnologías Utilizadas

| Categoría | Librería/Tool | Versión | Propósito |
|-----------|--------------|---------|-----------|
| **UI** | Jetpack Compose | Latest | UI declarativa |
| | Material 3 | Latest | Componentes de UI |
| | Coil | Latest | Carga de imágenes |
| **Networking** | Retrofit | 2.9+ | Cliente HTTP |
| | Gson | 2.10+ | Serialización JSON |
| **Async** | Kotlin Coroutines | 1.7+ | Programación asíncrona |
| | StateFlow | Latest | Estado reactivo |
| **Architecture** | ViewModel | Latest | MVVM |
| | Lifecycle | Latest | Manejo de ciclo de vida |
| **Backend** | FastAPI | Latest | API REST (Python) |

