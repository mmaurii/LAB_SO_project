# Laboratorio di Sistemi Operativi A.A. 2023-24
- Nome gruppo: I Tre Romagnoli
- Mail referente: maurizio.amadori4@studio.unibo.it
- Componenti gruppo:
  - Maurizio Amadori `0001078717`
  - Magrini Lorenzo `0001070628`
  - Alessandro Nanni `0001027757`

## Architettura generale
Il paradigma di alto livello implementato è di tipo client – server. Il client si preoccupa di ottenere da console i comandi e i messaggi dell’utente e di spedirli al server, andando poi a mostrare le risposte che quest’ultimo gli fornisce. Il server invece si preoccupa di ricevere messaggi e comandi verificarne la validità e dare una risposta consona al client. Il server stesso offre un’interfaccia a linea di comando che permette a un utente di svolgere operazioni su quest’ultimo in maniera sicura. Senza intaccare quindi il corretto svolgimento di operazione da parte del server su richiesta del client.

![classi.svg](classi.svg)

## Meccanismi Applicazione
### Sever
#### Overview
La classe server ha una ServerSocket che permette ai client di connettersi ad esso. Nel suo main thread resta in ascolto per i comandi da console. Dispone di un thread separato in cui accetta i client in arrivo. Ogni volta che un client si connette viene creato un ClientHandler associato ad esso, il quale viene aggiunto alla lista dei client. Uno Scanner viene usato per inviare comandi da tastiera, che vengono poi differenziati tra comandi in modalità ispezione e non.

### ClientHandler
#### Overview
Il ClientHandler gestisce la comunicazione tra il sever e i client connessi. Ogni ClientHandler dispone di un proprio thread che permette di gestire più richieste dei client contemporaneamente. La classe ClientHandler ha un riferimento alla classe server a cui è connesso il client, che usa per ricevere informazioni da esso e modificarne i suoi contenuti (Topic, Messaggi).

### Client
#### Overview
Un client viene avviato fornendogli un host e una porta, che formano la socket che usa per connettersi al server. Il client usa le classi Sender e Receiver per inviare e ricevere messaggi al server rispettivamente (tramite il ClientHandler). Le funzioni di invio e di ricezione di queste ultime avviene su due thread indipendenti affinche un client possa ricevere e inviare dati contemporaneamente.

### Sender
#### Overview
La classe Sender dispone di una socket (la stessa del Client) che usa per inviare messaggi al server. L'utente ha acesso ai comandi del client tramite un BufferedReader.

### Receiver
#### Overview
La classe Receiver dispone di una socket (la stessa del Client) che usa per inviare messaggi al server, e un riferimento al Sender associato. Il Reciever informa il Sender che deve interrompersi quando il client non è più connesso al server.

### Topic
#### Overview
Un topic è formato da un titolo e da una lista di messaggi su quel determinato topic.

### Message
#### Overview
Un messaggio dispone di un id, il testo del messaggio e la data di invio.

### Command
#### Overview
Quando il server è in fase di ispezione e un client prova ad inviare un comando ad esso, viene creato un nuovo oggetto Command che contiene il comando, eventuali parametri e il ClientHandler che lo ha inviato. Il nuovo comando viene poi aggiunto a una lista di comandi in attesa nel server.


## Suddivisione compiti *da aggiornare*
### Magrini
- classe messaggio e client
- implementazione della struttura dei messaggi di output
### Nanni
- classe server
- logiche di comunicazione del paradigma client - server
### Amadori
- classe client, utente e topic
- Logiche di comunicazione del paradigma client - server


## Problemi e ostacoli
### Scanner era bloccante
ecc ecc
## Strumenti usati per l'organizzazione
- Editor: IntelliJ Community Edition
- Repository codice: Github
- Comunicazione: Discord e Whatsapp
## Requisiti e Istruzioni