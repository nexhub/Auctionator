# Auctionator

[[english version](./README.md)]

Ist eine Web-Anwendung zum Versteigern (Aufteilen) finanzieller Lasten auf eine Gruppe von Menschen.

Zum Beispiel f&uuml;r eine Mietversteigerung.

---
## Anmerkungen

* Es wird [Java](https://java.com/de/download) mindestens in der Version 8 vorausgesetzt.


* Die Web-Anwendung wurde speziell für eine Mietversteigerung in einem [Mietshaeuser Syndikats](https://www.syndikat.org) Hausprojekt entwickelt. 


* Es wurde bisher kein Augenmerk auf universelle Einsatzbereit oder umfängliche Dokumentation gelegt.


* Ein nachtr&auml;gliches editieren einer Auktion ist nicht vorgesehen, Das Manipulieren der SQLite-Auktions-Datei mit einem externen Tool ([z.B.](http://sqlitebrowser.org/)) w&auml;re eine M&ouml;glichkeit.


* Schaut ob ihr damit was anfangen k&ouml;nnt. Entwickelt es gerne Weiter. Viel Spa&szlig; damit. ;)


---
## Benutzung

* Download: [https://github.com/qdev/Auctionator/releases/download/v0.1/auctionator-0.1.jar](https://github.com/qdev/Auctionator/releases/download/v0.1/auctionator-0.1.jar)


* Auktions-Datei (sqlite) erstellen

  mit: `java -jar auctionator.jar` **DATEINAME** `CREATE` **ZIEL** **PERSON1** **PERSON2** **...**
  
  z.B: `java -jar auctionator.jar auktion1.db CREATE 5000 Anne Max 'Alex P.'`
  
  Dies erzeugt im aktuellen Verzeichnis eine Aution-Datei mit dem Namen Auktion1.db f&uuml;r 3 Bieter_innen mit dem Ziel 5000 Euro aufzutreiben.
  
* Die Web-Anwendung starten
  
  mit: `java [-Dserver.port=PORT] -jar auctionator.jar` **DATEINAME** `START`
  
  Die Angabe einses Ports ist optional. Default ist 10042. Somit wird das obere Beispiel gestartet
  
  mit: `java -jar auctionator.jar auktion1.db START`
  
* Die Web-Anwendung aufrufen
  
  Im Browser `http://`**SERVERNAME**`:`**PORT**`/auction/` aufrufen.
  
  In dem Beispiel also: `http://localhost:10042/auction/`
  
  Dort k&ouml;nnen dann die einzelen Personen ihre Gebote Eingeben 
  
  Dort k&ouml;nnen dann von den einzelnen Personen ihre Gebote Eingeben werden. Das letzte Gebot beendet immer die aktuelle Runde und erzeugt eine neue.
  
---
## Hinweise f&uuml;r Entwickler_innne

* Es ist ein [Maven](https://maven.apache.org/) Projekt  

