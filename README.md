# Rush Hour NxM (Java 21 Maven Projekt)

Kétszemélyes, parancssoros stratégiai csúszó-blokk játék Java 21-ben, a feladatkiírás összes követelményének megfelelően.

## Projekt megnyitása IntelliJ IDEA-ban
1. Csomagold ki a ZIP fájlt egy tetszőleges mappába (pl. `rush-hour`).
2. Nyisd meg az **IntelliJ IDEA**-t -> **File -> Open...** -> válaszd ki a kicsomagolt mappán belüli `pom.xml` fájlt -> **Open as Project**.
3. Várd meg, amíg a Maven letölti a függőségeket (JUnit 5, Mockito, Logback, H2).
4. Futtatás közvetlenül az IDE-ből: nyisd meg a `hu.projekt.rushhour.Main` osztályt és kattints a zöld **Run** ikonra.

## Build és tesztelés parancssorból (`mvn clean install`)
```bash
mvn clean install
```
A build automatikusan lefuttatja:
- a **Checkstyle** kódformázási és minőségi ellenőrzést (`maven-checkstyle-plugin`),
- a **JUnit 5 + Mockito** egységteszteket,
- a **JaCoCo** kódlefedettség-ellenőrzést (minimum 80% üzleti logikára, riport: `target/site/jacoco/index.html`),
- és elkészíti a függőségeket is tartalmazó futtatható JAR fájlt (`target/rush-hour-1.0-SNAPSHOT-jar-with-dependencies.jar`).

Futtatás JAR-ból:
```bash
java -jar target/rush-hour-1.0-SNAPSHOT-jar-with-dependencies.jar
```
Opcionálisan külső pályafájl is megadható parancssori argumentumként:
```bash
java -jar target/rush-hour-1.0-SNAPSHOT-jar-with-dependencies.jar utvonal/sajat-palya.txt
```

## Játékmenet és parancsok
- **Humán játékos (`A` céljármű)**: a kijelölt kijáraton (alapértelmezetten jobb szél, 3. sor) kell kicsúsztatnia az `A` járművet.
- **Gépi ellenfél (`B` céljármű)**: a saját kijáratán (alapértelmezetten bal szél, 4. sor) próbálja kivezetni a `B` járművet.
- A soron lévő játékos **bármelyik járművet** (`A`, `B`, vagy semleges akadályt) elcsúsztathatja a tengelye mentén 1 vagy több üres mezővel („hosszú lépés”).
- **Parancsok:**
  - `<JÁRMŰ> <IRÁNY> <LÉPÉSSZÁM>` – pl. `A RIGHT 2`, `C UP 1`, `D BALRA 3`
  - `LOAD <fajlnev.txt>` – pálya betöltése szövegfájlból
  - `SAVE <fajlnev.txt>` – aktuális pálya mentése szövegfájlba
  - `SAVE_XML <fajlnev.xml>` – teljes játékállás mentése XML-be (opcionális +pont)
  - `LOAD_XML <fajlnev.xml>` – mentett játék visszatöltése XML-ből
  - `SCORES` – H2 adatbázisban tárolt High Score ranglista megtekintése
  - `HELP` – súgó
  - `QUIT` – kilépés


