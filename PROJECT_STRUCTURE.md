# Ragnarok Database - Estructura del Proyecto

## Resumen

Este proyecto implementa una arquitectura **MVVM (Model-View-ViewModel)** para una aplicación Android que consume una API REST de Ragnarok Online. La app muestra items populares del juego con filtros por período de tiempo.

## Estructura de Carpetas

```
com.example.ragnarokdatabase/
├── data/                           # Capa de datos
│   ├── remote/                    # Servicios remotos (API)
│   │   ├── NetworkModule.kt      # ✅ Configuración de Retrofit + Gson
│   │   └── RagnarokApiService.kt # ✅ Interface de endpoints de la API
│   └── repository/                # Repositorios
│       ├── ItemRepository.kt     # ✅ Repositorio de Items
│       └── MonsterRepository.kt  # ✅ Repositorio de Monsters
│
├── model/                          # Modelos de datos
│   ├── Item.kt                    # ✅ Modelos: Item, ItemStats, PopularItem, PopularItemsResponse
│   └── Monster.kt                 # ✅ Modelo de Monster con MonsterStats y MonsterDrop
│
├── ui/                             # Componentes de UI
│   └── theme/                     # Temas de la aplicación
│       ├── Color.kt              # ✅ Colores de Tailwind CSS
│       ├── Theme.kt              # ✅ Tema oscuro (Dark mode)
│       └── Type.kt               # ✅ Tipografía con Lilita One
│
├── view/                           # ✅ Composables de UI
│   └── MainScreen.kt              # ✅ Pantalla principal con items populares
│       ├── MainScreen()           # Composable principal
│       ├── PeriodFilterChips()    # Filtros por período
│       ├── PopularItemCard()      # Card de item individual
│       └── ErrorCard()            # Manejo de errores
│
├── viewmodel/                      # ✅ ViewModels
│   └── MainViewModel.kt           # ✅ ViewModel de la pantalla principal
│       └── MainUiState            # Sealed class: Loading, Success, Error
│
└── MainActivity.kt                 # ✅ Actividad principal con Compose
```

## Configuración de la API

### Base URL
```
Emulador: http://10.0.2.2:8000/api/v1/
Host PC:  http://127.0.0.1:8000/api/v1/
```

> **Nota**: El emulador Android usa `10.0.2.2` para acceder al `localhost` del host.

### Endpoints Implementados

#### Items Populares
- **GET** `/items/popular/{period}`
- **Parámetros**:
  - `period`: `today` | `yesterday` | `last7days` | `last30days`
  - `limit`: Int (query param, default 10)
- **Ejemplo**: http://127.0.0.1:8000/api/v1/items/popular/today?limit=10
- **Respuesta**: 
  ```json
  {
    "period": "today",
    "items": [
      {
        "item_id": 501,
        "name": "Red Potion",
        "type": "Healing",
        "view_count": 3,
        "sprite": "red_potion"
      }
    ]
  }
  ```

#### Items (Detalle)
- **GET** `/items/{id}`
- **Ejemplo**: http://127.0.0.1:8000/api/v1/items/501
- **Respuesta**: Información completa del item incluyendo stats, precios, requisitos, etc.

#### Monsters (Detalle)
- **GET** `/monsters/{id}`
- **Ejemplo**: http://127.0.0.1:8000/api/v1/monsters/1002
- **Respuesta**: Información completa del monstruo incluyendo stats, drops, spawn locations, etc.

## Tecnologías y Librerías

### Inyección de Dependencias
- **Manual**: Sin framework DI (instancias creadas con defaults)
- NetworkModule es un `object` singleton
- Repositorios reciben API service por parámetro

### Networking
- **Retrofit 2.9+**: Cliente HTTP type-safe
- **Gson 2.10+**: Parser JSON (no Moshi)
- **Suspend functions**: Todas las llamadas API son `suspend fun`

### UI
- **Jetpack Compose**: UI moderna y declarativa
- **Material3**: Diseño Material Design 3
- **Coil**: Carga de imágenes desde URL
- **Lilita One Font**: Fuente personalizada para headers

### Arquitectura
- **MVVM**: Model-View-ViewModel sin Hilt
- **Coroutines**: Para operaciones asíncronas con `viewModelScope`
- **StateFlow**: Para manejo de estado reactivo (no Flow<Result<T>>)
- **Sealed Classes**: Para estados tipados (`MainUiState`)

### Configuración
- **minSdk**: 31 (Android 12)
- **targetSdk**: 36
- **Java**: Version 11
- **Kotlin**: Latest

## Uso de los Repositorios

### ItemRepository

```kotlin
// En MainViewModel
class MainViewModel(
    private val itemRepository: ItemRepository = ItemRepository()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
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
}
```

### MonsterRepository

```kotlin
// Ejemplo de uso futuro
class MonsterViewModel(
    private val monsterRepository: MonsterRepository = MonsterRepository()
) : ViewModel() {
    
    private val _monsterState = MutableStateFlow<MonsterState>(MonsterState.Loading)
    val monsterState: StateFlow<MonsterState> = _monsterState.asStateFlow()
    
    fun loadMonster(monsterId: Int) {
        viewModelScope.launch {
            _monsterState.value = MonsterState.Loading
            try {
                val monster = monsterRepository.getMonster(monsterId)
                _monsterState.value = MonsterState.Success(monster)
            } catch (e: Exception) {
                _monsterState.value = MonsterState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}
```

### Observar en UI con Compose

```kotlin
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    
    when (uiState) {
        is MainUiState.Loading -> {
            CircularProgressIndicator()
        }
        is MainUiState.Success -> {
            val items = (uiState as MainUiState.Success).items
            LazyColumn {
                items(items) { item ->
                    PopularItemCard(item = item)
                }
            }
        }
        is MainUiState.Error -> {
            ErrorCard(message = (uiState as MainUiState.Error).message)
        }
    }
}
```

## Modelos de Datos

### Item
- **id**: Int
- **name**: String
- **description**: String
- **type**: String (e.g., "Healing")
- **subtype**: String? (opcional)
- **buyPrice**: Int
- **sellPrice**: Int
- **stats**: ItemStats (atk, matk, defense, weight, slots)
- **requiredLevel**: Int
- **requiredJob**: String? (opcional)
- **gender**: String? (opcional)
- **location**: String? (opcional)
- **sprite**: String

### PopularItem (usado en MainScreen)
- **id**: Int (mapeado desde `item_id`)
- **name**: String
- **type**: String
- **viewCount**: Int (mapeado desde `view_count`)
- **sprite**: String?
- **getIconUrl()**: String - Genera URL de imagen desde Divine Pride

### PopularItemsResponse (wrapper del backend)
- **period**: String (today, yesterday, last7days, last30days)
- **items**: List<PopularItem>

### Monster
- **id**: Int
- **name**: String
- **level**: Int
- **element**: String (e.g., "Water")
- **elementLevel**: Int
- **race**: String (e.g., "Plant")
- **size**: String (e.g., "Medium")
- **stats**: MonsterStats (hp, sp, exp, atk, def, stats primarios)
- **drops**: List<MonsterDrop> (items que dropea)
- **mvp**: Boolean
- **mvpDrops**: List<MonsterDrop>? (opcional)
- **spawnLocations**: List<String>? (opcional)
- **sprite**: String

## Permisos Requeridos

El AndroidManifest.xml incluye:
- `INTERNET`: Para realizar peticiones HTTP
- `ACCESS_NETWORK_STATE`: Para verificar conectividad
- `usesCleartextTraffic="true"`: Para permitir tráfico HTTP (desarrollo local)

## Estado Actual del Proyecto

### ✅ Implementado
- [x] MainScreen con lista de items populares
- [x] Filtros por período (today, yesterday, last7days, last30days)
- [x] PopularItemCard con imagen (Coil) y nombre
- [x] MainViewModel con StateFlow y manejo de estados
- [x] ItemRepository con getPopularItems
- [x] MonsterRepository con getMonster
- [x] NetworkModule con Retrofit + Gson
- [x] RagnarokApiService con endpoints
- [x] Modelos: Item, Monster, PopularItem, PopularItemsResponse
- [x] Tema Dark con colores Tailwind CSS
- [x] Fuente personalizada Lilita One para headers
- [x] Manejo de errores con ErrorCard

### 🚧 Próximos Pasos

1. **Pantallas de Detalle**
   - [ ] ItemDetailScreen con toda la información del item
   - [ ] MonsterDetailScreen con stats y drops

2. **Navegación**
   - [ ] Implementar Jetpack Navigation Compose
   - [ ] Navegación desde PopularItemCard a ItemDetailScreen

3. **Búsqueda**
   - [ ] SearchScreen para buscar items y monsters
   - [ ] Filtros avanzados por tipo, nivel, etc.

4. **Caché Local**
   - [ ] Room Database para favoritos
   - [ ] Modo offline con caché de consultas

5. **Testing**
   - [ ] Unit tests para ViewModels
   - [ ] Integration tests para Repositories
   - [ ] UI tests para Composables

6. **Mejoras Arquitectónicas**
   - [ ] Implementar Hilt o Koin para DI
   - [ ] Mejorar error handling con tipos específicos
   - [ ] Agregar logging con Timber

## Flujo de Datos Actual

```
User Click (Period Filter)
    ↓
MainScreen calls viewModel.loadPopularItems(period)
    ↓
MainViewModel.viewModelScope.launch
    ↓
ItemRepository.getPopularItems(period, limit)
    ↓
RagnarokApiService.getPopularItems(period, limit) [suspend fun]
    ↓
Retrofit HTTP GET → http://10.0.2.2:8000/api/v1/items/popular/{period}
    ↓
FastAPI Backend responde JSON
    ↓
Gson deserializa → PopularItemsResponse
    ↓
Repository extrae .items → List<PopularItem>
    ↓
ViewModel actualiza _uiState.value = Success(items)
    ↓
StateFlow emite → MainScreen.collectAsState() recibe
    ↓
Compose recompone → LazyColumn muestra PopularItemCard
    ↓
Coil carga imágenes desde Divine Pride
```

