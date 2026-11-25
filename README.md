# Przepiśnik — Katalog przepisów kulinarnych

## Etap 1 — Interfejs użytkownika

### Opis projektu
**Przepiśnik** to wieloplatformowa aplikacja mobilna (Kotlin Multiplatform / Compose Multiplatform) służąca do przeglądania, dodawania i organizowania przepisów kulinarnych. Aplikacja umożliwia wyszukiwanie dań po nazwie lub składnikach, filtrowanie według kategorii oraz oznaczanie ugotowanych potraw. Projekt skierowany jest do osób gotujących w domu, studentów oraz każdego, kto chce mieć własną bazę szybkich przepisów zawsze pod ręką (również bez dostępu do Internetu).

### Ekrany aplikacji (5 ekranów)
1. **EkranListy** — lista przepisów z polem wyszukiwania, filtrami kategorii (`FilterChip`), filtrem „Tylko ugotowane”, przyciskiem dodawania (`+`) oraz obsługą stanów ładowania, błędu i braku danych.
2. **EkranSzczegolow** — szczegółowy widok wybranego przepisu (kategoria, czas przygotowania, składniki), przełącznik statusu „Oznacz jako ugotowane” oraz usuwanie z potwierdzeniem.
3. **EkranFormularza** — formularz dodawania nowego przepisu (nazwa, składniki, czas przygotowania w minutach, wybór kategorii) z walidacją pól i przewijaniem.
4. **EkranStatystyk** — podsumowanie kolekcji: łączna liczba przepisów, liczba ugotowanych, średni czas przygotowania, pasek postępu (`LinearProgressIndicator`) oraz rozbicie na kategorie.
5. **EkranUstawien** — ustawienia aplikacji zapisywane trwale w DataStore (kolejność sortowania, maksymalny czas przygotowania), ręczne odświeżanie z API oraz usuwanie wszystkich danych użytkownika.

### Model danych
* **Kategoria**: `id: Int`, `nazwa: String`.
* **Przepis**: `id: Int`, `nazwa: String`, `skladniki: String`, `czasMinuty: Int`, `kategoriaId: Int`, `kategoriaNazwa: String`, `ugotowane: Boolean`.

### Dostępność
1. Wszystkie rozmiary czcionek zdefiniowano w jednostkach `sp` w `AppTheme`, dzięki czemu skalują się z ustawieniami systemowymi.
2. Zastosowano wyłącznie kolory z palety `MaterialTheme.colorScheme` (pary `primary`/`onPrimary`, `surfaceVariant`/`onSurfaceVariant`) zapewniające czytelny kontrast w trybie jasnym i ciemnym.
3. Informacje o błędach i statusach nie opierają się wyłącznie na kolorze — zawsze towarzyszy im jednoznaczny komunikat tekstowy.
4. Elementy interaktywne (`Button`, `FloatingActionButton`, `FilterChip`, `Switch`, `NavigationBarItem`) spełniają minimalny obszar dotyku `48x48 dp`.
5. Ekrany formularza, szczegółów i statystyk korzystają z przewijania (`verticalScroll`), zapobiegając ucinaniu tekstu przy powiększonej czcionce, a układ listy i statystyk dostosowuje się do szerokich ekranów (`BoxWithConstraints`, próg `600.dp`).

---

## Etap 2 — Cykl życia aplikacji

### Obserwacja cyklu życia (`ObserwatorCyklu`)
Komponent `ObserwatorCyklu` został podpięty w `App`, `EkranListy`, `EkranSzczegolow`, `EkranFormularza`, `EkranStatystyk` oraz `EkranUstawien`.
* **Wniosek 1:** Zdarzenia `ON_START`, `ON_RESUME`, `ON_PAUSE` i `ON_STOP` są wspólne dla wszystkich aktywnych ekranów, ponieważ pochodzą z cyklu życia nadrzędnej aktywności (`MainActivity`).
* **Wniosek 2:** Zdarzenia `kompozycja dodana` / `kompozycja usunięta` (`DisposableEffect`) występują niezależnie dla każdego ekranu podczas przełączania zakładek, otwierania formularza lub obrotu ekranu.

### Inwentaryzacja stanu aplikacji
| Ekran | Zmienna | Typ | Rodzaj stanu | Miejsce przechowywania |
| :--- | :--- | :--- | :--- | :--- |
| `App` | `zakladka` | `Int` | stan interfejsu | `rememberSaveable` |
| `App` | `pokazFormularz` | `Boolean` | stan interfejsu | `rememberSaveable` |
| `App` | `wybranyId` | `Int?` | stan interfejsu | `rememberSaveable` |
| `EkranListy` | `filtry` | `KryteriaFiltrowania` | stan interfejsu | `rememberSaveable` + `KryteriaFiltrowaniaSaver` |
| `EkranListy` | `doUsuniecia` | `Przepis?` | stan przejściowy | `remember` |
| `EkranFormularza` | `dane` | `DaneFormularza` | stan interfejsu | `rememberSaveable` + `DaneFormularzaSaver` |
| `EkranFormularza` | `pokazBledy` | `Boolean` | stan interfejsu | `rememberSaveable` |
| `CzasWAplikacji` | `sekundy` | `Int` | stan interfejsu | `rememberSaveable` |
| `PrzepisyViewModel` | `stan` | `StateFlow<StanPrzepisow>` | dane aplikacji | `ViewModel` + `Room` + `DataStore` |

### Własny Saver
Zastosowano dwa własne Savery (`listSaver`): `KryteriaFiltrowaniaSaver` (3 pola: `fraza`, `kategoriaId`, `tylkoUgotowane`) oraz `DaneFormularzaSaver` (4 pola: `nazwa`, `skladniki`, `czasTekst`, `kategoriaId`). `rememberSaveable` automatycznie zapisuje wyłącznie typy proste (`String`, `Int`, `Boolean`), dlatego własne klasy `data class` wymagają jawnego określenia sposobu zamiany na listę wartości prostych i odtworzenia obiektu.

### Reakcja na przejście do tła
Komponent `CzasWAplikacji` korzysta z `LifecycleEventEffect(Lifecycle.Event.ON_START)` oraz `ON_STOP`, aby wstrzymywać odliczanie czasu, gdy aplikacja znajduje się w tle.

### Porównanie Android i iOS
Na Androidzie obrót ekranu domyślnie niszczy i odtwarza `Activity` (`onPause → onStop → onDestroy → onCreate → onStart → onResume`), przez co stan w `remember` jest tracony, a zachowany zostaje tylko stan w `rememberSaveable` i `ViewModel`. Na iOS zmiana orientacji ekranu nie niszczy kontrolera widoku, więc stan interfejsu zwykle pozostaje w pamięci. Przejście aplikacji do tła na Androidzie wywołuje `onPause` i `onStop`, a na iOS odpowiada zdarzeniom `willResignActive` (`ON_PAUSE`) i `didEnterBackground` (`ON_STOP`). Dzięki bibliotece `lifecycle-runtime-compose` kod w `commonMain` reaguje na te same zdarzenia na obu platformach. W przypadku zabicia procesu przez system operacyjny dane aplikacji na obu platformach muszą być zapisane w pamięci trwałej (Room / DataStore).

---

## Etap 3 — Architektura aplikacji (MVVM)

### Podział na warstwy i kontrola zależności
* **Warstwa UI (`App`, `EkranListy`, `EkranSzczegolow`, `EkranFormularza`, `EkranStatystyk`, `EkranUstawien`)** — wyświetla `StanPrzepisow` i przekazuje zdarzenia w górę. Nie importuje repozytorium ani encji bazy danych.
* **Warstwa stanu (`PrzepisyViewModel`, `StanPrzepisow`)** — łączy strumienie danych w jeden `StateFlow` (`stateIn` z `SharingStarted.WhileSubscribed(5_000)`). Nie zawiera żadnych importów z `androidx.compose`.
* **Warstwa danych (`PrzepisyRepozytorium`, `PrzepisyRepozytoriumOffline`, `PrzepisyApi`, `BazaDanych`, `UstawieniaDataStore`)** — ukrywa źródła danych za interfejsami. Obiekty warstw tworzone są w jednym miejscu (`App.kt`) i wstrzykiwane przez konstruktor.

### Droga zdarzenia (dodanie przepisu)
| Krok | Element | Co się dzieje | Kierunek |
| :--- | :--- | :--- | :--- |
| 1 | `EkranFormularza` | Użytkownik klika „Zapisz przepis”, walidacja przechodzi, wywoływane jest `onZapisz` | zdarzenie w górę |
| 2 | `App` | Przekazuje wywołanie do `viewModel.dodaj(...)` | zdarzenie w górę |
| 3 | `PrzepisyViewModel` | Uruchamia korutynę w `viewModelScope` i wywołuje `repozytorium.dodaj(...)` | zdarzenie w dół |
| 4 | `PrzepisyRepozytoriumOffline` | Wysyła żądanie POST do API i zapisuje encję w tabeli `przepisy` (Room) | zmiana danych |
| 5 | `PrzepisyViewModel` → `EkranListy` | Strumień `Flow` z Room emituje nową listę, aktualizuje się `StateFlow`, UI przerysowuje listę | stan w górę |

### Zachowanie danych
* **Po obrocie ekranu:** lista przepisów (z `ViewModel`/Room) oraz wpisane dane w formularzu i filtry (`rememberSaveable`) pozostają zachowane.
* **Po zabiciu procesu:** stan nawigacji i formularza odtwarza `rememberSaveable`, a przepisy i ustawienia wczytują się natychmiast z lokalnej bazy Room i DataStore.

---

## Etap 4 — Integracja z API

### Opis wybranego API
* **Nazwa:** DummyJSON Recipes API
* **Adres bazowy:** `https://dummyjson.com`
* **Uwierzytelnienie:** brak (publiczne API testowe, bez klucza API)
* **Limity i licencja:** darmowe API do celów edukacyjnych i prototypowania (MIT).
* **Punkty końcowe:**
  * `GET /recipes?limit=15` → `PrzepisyApi.pobierz()` (pobranie listy przepisów)
  * `POST /recipes/add` → `PrzepisyApi.dodaj(...)` (utworzenie nowego przepisu; serwer symuluje zapis i zwraca utworzony obiekt JSON)
* **Uzasadnienie wyboru:** DummyJSON Recipes zwraca realistyczne dane kulinarne zawierające nazwę, listę składników, czas przygotowania i typ posiłku, które bezpośrednio odpowiadają modelowi aplikacji Przepiśnik. Serwis obsługuje zarówno odczyt (`GET`), jak i zapis (`POST`) bez konieczności rejestracji i przechowywania poufnych kluczy. Pozwala to w pełni przetestować serializację, mapowanie DTO oraz obsługę błędów sieciowych.

### Przykładowa odpowiedź JSON (`GET /recipes?limit=1`)
```json
{
  "recipes": [
    {
      "id": 1,
      "name": "Classic Margherita Pizza",
      "ingredients": ["Pizza dough", "Tomato sauce", "Fresh mozzarella cheese"],
      "prepTimeMinutes": 20,
      "cookTimeMinutes": 15,
      "mealType": ["Dinner"]
    }
  ]
}
```

### Konfiguracja klienta HTTP (Ktor)
* `connectTimeoutMillis = 5_000` (5 s) — szybkie wykrycie braku połączenia lub niedostępnego hosta.
* `requestTimeoutMillis = 10_000` (10 s) — bezpieczny zapas na odpowiedź serwera w sieci mobilnej.
* `expectSuccess = true` oraz `Json { ignoreUnknownKeys = true }`.

### Obsługa błędów sieciowych
| Scenariusz | Oczekiwane zachowanie | Zaobserwowane zachowanie |
| :--- | :--- | :--- |
| Tryb samolotowy (brak sieci) | Komunikat „Brak połączenia z Internetem.”, przycisk „Spróbuj ponownie” lub wyświetlenie lokalnych danych z komunikatem | Zgodne z oczekiwanym |
| Przekroczenie czasu (`HttpRequestTimeoutException`) | Komunikat „Serwer nie odpowiada. Spróbuj ponownie.” | Zgodne z oczekiwanym |
| Błąd klienta 4xx / serwera 5xx (`ClientRequestException` / `ServerResponseException`) | Komunikat z kodem HTTP w języku polskim | Zgodne z oczekiwanym |
| Błędny format JSON (`ContentConvertException`) | Komunikat „Odpowiedź serwera ma nieoczekiwany format.” | Zgodne z oczekiwanym |

### Bezpieczeństwo danych dostępowych
Kod źródłowy i repozytorium nie zawierają żadnych kluczy API, haseł ani tokenów. Umieszczenie klucza bezpośrednio w kodzie aplikacji mobilnej wiąże się z ryzykiem jego łatwego wyodrębnienia poprzez dekompilację pliku APK i nieautoryzowanego wykorzystania limitów lub konta przez osoby trzecie.

---

## Etap 5 — Dane trwałe (Room 3 + DataStore)

### Schemat bazy danych
Baza danych Room 3 (`przepisnik.db`, wersja `1`, schemat eksportowany do `shared/schemas`) składa się z dwóch powiązanych tabel w relacji **1:N**:
1. **`kategorie`** (`id: Int PK`, `nazwa: String`) — przechowuje kategorie posiłków (Śniadanie, Obiad, Kolacja, Deser).
2. **`przepisy`** (`id: Int PK autoGenerate`, `nazwa: String`, `skladniki: String`, `czasMinuty: Int`, `kategoriaId: Int FK → kategorie.id`, `ugotowane: Boolean`) — przechowuje przepisy; indeks na `kategoriaId`.

Dla klucza obcego zastosowano `ForeignKey.CASCADE` — usunięcie kategorii automatycznie usuwa przypisane do niej przepisy, co zachowuje spójność referencyjną bazy.

### Tryb offline-first i konflikty
Jedynym źródłem prawdy dla interfejsu jest lokalna baza Room obserwowana przez `Flow`. Dane pobierane z API trafiają najpierw do bazy (`wstawWszystkie` z `OnConflictStrategy.REPLACE`), a interfejs odświeża się automatycznie.
* **Reguła konfliktów:** rekordy z serwera o tym samym `id` nadpisują lokalne wiersze.
* **Ograniczenie:** lokalne oznaczenie przepisu z API jako „ugotowane” lub jego usunięcie zostanie nadpisane przy ponownym pobraniu tej samej puli z API.

### Scenariusze pracy offline
| Scenariusz | Oczekiwane zachowanie | Zaobserwowane zachowanie |
| :--- | :--- | :--- |
| Uruchomienie aplikacji w trybie samolotowym po wcześniejszej synchronizacji | Natychmiastowe wyświetlenie zapisanych przepisów z Room + komunikat o braku sieci | Zgodne z oczekiwanym |
| Dodanie nowego przepisu i zmiana ustawień w DataStore bez sieci, a następnie restart aplikacji | Nowy przepis, kolejność sortowania i limit czasu są zachowane po ponownym uruchomieniu | Zgodne z oczekiwanym |

### Ustawienia użytkownika (DataStore Preferences)
* `najnowsze_najpierw` (`Boolean`, domyślnie `true`) — przełącznik kolejności wyświetlania przepisów.
* `maks_czas_minuty` (`Int`, domyślnie `120`) — suwak filtrujący przepisy według maksymalnego czasu przygotowania.

### Prywatność i usuwanie danych
* **Klasyfikacja danych:** aplikacja przechowuje wyłącznie **dane zwykłe** (nazwy przepisów, składniki, czas przygotowania, preferencje sortowania). Nie zbiera żadnych danych osobowych ani wrażliwych.
* **Usuwanie danych:** na ekranie `Ustawienia` znajduje się przycisk „Usuń wszystkie dane użytkownika” z oknem potwierdzenia (`AlertDialog`), który czyści tabelę `przepisy` w Room oraz wszystkie preferencje w DataStore, przywracając aplikację do stanu początkowego.