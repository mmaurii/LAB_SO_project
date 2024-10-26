# Laboratorio di Sistemi Operativi A.A. 2023-24
- Nome gruppo: I Tre Romagnoli
- Mail referente: maurizio.amadori4@studio.unibo.it
- Componenti gruppo:
  - Maurizio Amadori `0001078717`
  - Magrini Lorenzo `0001070628`
  - Alessandro Nanni `0001027757`

## Architettura generale
Il paradigma di alto livello implementato è di tipo client - server. Il client si preoccupa di ottenere da console i comandi e i messaggi dell'utente e di spedirli al server, andando poi a mostrare le risposte che quest'ultimo gli fornisce. Il server invece si preoccupa di ricevere messaggi e comandi verificarne la validità e dare una risposta consona al client.  
Il server stesso offre un'interfaccia a linea di comando che permette a un utente di svolgere operazioni su quest'ultimo in maniera sicura. Senza intaccare quindi il corretto svolgimento di operazione da parte del server su richiesta del client.

![classi.svg](classi.svg)

## Meccanismi Applicazione

### Server
#### Overview
La classe server ha una ServerSocket che permette ai client di connettersi a esso. Nel suo main thread resta in ascolto per i comandi da console. Dispone di un thread separato in cui accetta i client in arrivo. Ogni volta che un client si connette viene creato un ClientHandler associato a esso, il quale viene aggiunto alla lista dei client. Uno Scanner viene usato per inviare comandi da tastiera, che vengono poi differenziati tra comandi in modalità ispezione e non.
#### Invio comandi
Il main thread della classe server usa uno Scanner per ricevere comandi da tastiera. Dopo aver fatto il parsing del comando viene controllato l'`inspectedTopic` del server. Se è `null`, il server accetta solo i comandi quit, show e inspect. Se invece sta ispezionando un topic potranno essere eseguiti solo i comandi listall, end e delete. 
#### Connessione client
Dato che il server implementa la classe `Runnable`, può eseguire un altro thread al suo interno, che usa la `serverSocket` per connettere i client a esso. I client connessi sono memorizzati nel server in una lista di ClientHandler. Quando viene inviato il comando quit, oltre che chiudere la socket del server, si "forzano" tutti i ClientHander a inviare il comando quit per scollegarli dal server prima che questo venga chiuso.
#### Gestione concorrenza

### SocketListener
#### Overview
La classe SocketListener gestisce le connessioni in entrata da parte dei client verso un server, utilizzando un thread dedicato che ascolta continuamente su una porta specifica per nuove connessioni.
#### Main Loop
Il server rimane in ascolto di nuove connessioni client utilizzando `serverSocket.accept()`. Ogni volta che un client si connette, il server crea un oggetto ClientHandler per gestire la comunicazione con quel client in un nuovo thread.
Ogni client viene aggiunto alla lista di client gestita dal server tramite il metodo `addClient()` del server.

### ClientHandler
#### Overview
La classe ClientHandler gestisce la comunicazione tra il sever e i client connessi. Ogni ClientHandler dispone di un proprio thread che permette di gestire più richieste dei client contemporaneamente.   
Ogni ClientHandler ha un riferimento alla classe server a cui è connesso il client, che usa per ricevere informazioni da esso e modificarne i suoi contenuti (Topic, Messaggi).
#### Main Loop
Un ClientHandler avvia un thread per ogni client connesso al server, permettendo l'esecuzione in parallelo di più client. Uno Scanner riceve i messaggi inviati dal Sender tramite l'InputSteam della socket, che vengono processati.  
Vengono fatti dei controlli per assicurarsi che i comandi esclusivi ai Publisher non vengano eseguiti se il Client è sender o ancora deve inviare i comandi di `publish` o `subscribe`.
#### Gestione concorrenza 

### Client
#### Overview
Un client viene avviato fornendogli un host e una porta, che formano la socket che usa per connettersi al server. Il client usa le classi Sender e Receiver per inviare e ricevere messaggi al server rispettivamente (tramite il ClientHandler).   
Le funzioni di invio e di ricezione di queste ultime avviene su due thread indipendenti affinche un client possa ricevere e inviare dati contemporaneamente.
#### Arresto client
La socket del client viene chiusa solamente dopo che i thread del sender e del reciever terminano.

### Sender
#### Overview
La classe Sender dispone di un riferimento alla socket del Client che usa per inviare messaggi al server. L'utente scrive i comandi sul client tramite un BufferedReader.
#### Main Loop
Il loop principale viene eseguito fino a quanto il thread viene interrotto o il sender invia il comando quit. Se il thread viene interrotto invia automaticamente il comando quit al server e termina.

### Receiver
#### Overview
La classe Receiver dispone di una socket (la stessa del Client) che usa per inviare messaggi al server, e un riferimento al Sender associato. Il Reciever informa il Sender che deve interrompersi quando il client non è più connesso al server.
#### Main Loop
Viene creato un BufferedReader, che legge i dati provenienti dal server tramite la socket. Un InputStreamReader converte l'input byte-stream del socket in una stringa. La connessione viene chiusa quando il `readLine()` del BufferedReader è `null`. Oppure se viene ricevuto il comando quit dal server, che interrompe il ciclo con un `break;`.  
Alla fine del ciclo, nel blocco `finally`, viene interrotto anche il thread del Sender.
### Topic
#### Overview
Un topic è formato da un titolo e da una lista di messaggi su quel determinato topic.

### Message
#### Overview
Un messaggio dispone di un id, il testo del messaggio e la data di invio.

### Command
#### Overview
Quando il server è in fase di ispezione e un client prova a inviare un comando a esso, viene creato un nuovo oggetto Command che contiene il comando, eventuali parametri e il ClientHandler che lo ha inviato. Il nuovo comando viene poi aggiunto a una lista di comandi in attesa nel server.


## Suddivisione compiti *da aggiornare*
### Magrini
- Classi Message e Client
- Implementazione della struttura dei messaggi di output
### Nanni
- Classi Server e ClientHandler
- Logiche di comunicazione del paradigma client - server
### Amadori
- Classi Client, Sender, Reciever e Topic
- Logiche di comunicazione del paradigma client - server


## Problemi e ostacoli
### Scanner era bloccante
ecc ecc
## Strumenti usati per l'organizzazione
- Editor: IntelliJ Community Edition
- Repository codice: GitHub
- Comunicazione: Discord e Whatsapp
## Requisiti e Istruzioni
comandi e screenshot vari


## Esempi di esecuzione e output
`@` indica chi sta eseguendo il comando, `>` indica il testo inviato e `<` indica il testo ricevuto. Per motivi di chiarezza non sono stati inlcusi i prompt del server `> Inserisci comando` e `> Inserisci comando (ispezionando topic "<topic>")`.  
Registrazione di client come publisher
```
@ Client
> publish cibo
< Registrato come publisher al topic cibo
```
Esecuzione del comando subscribe per un publisher
```
@ Client(publisher)
> subscribe cibo
< Non puoi più eseguire questo comando
```
Invio di messaggio
```
@ Client(publisher)
> send
< Non puoi inviare un messaggio vuoto
> send carbonara
< Inviato messaggio "carbonara"
```
Ispezione di topic non esistente
```
@ Server
> inspect sport
< Il topic sport non esiste
```
Ispezione di topic esistente
```
@ Server
> inspect cibo
< Ispezionando il topic: cibo
```
Elenco di messaggi inviati sul topic
```
@ Server
> listall
< Sono stati inviati 1 messaggi in questo topic.
	- ID: 1
	  Testo: carbonara
	  Data: 26/10/2024 - 11:56:45
```
Client invia un comando quando il server è in fase di ispezione
```
@ Client(publishber)
> send carbonara 
< Messaggio "carbonara" in attesa. Il server è in fase d'ispezione.
```
Comando non riconosciuto in fase di ispezione
```
@ Server
> quit 
< Comando non riconosciuto: quit
```
Fine ispezione topic
```
@ Server
> end 
< Fine ispezione del topic cibo.
```
Ricezione messaggi con contenuto uguale durante fase di ispezione
```
@ Server
> inspect cibo
< Ispezionando il topic: cibo
> listall
< Sono stati inviati 2 messaggi in questo topic.
	- ID: 1
	  Testo: carbonara
	  Data: 26/10/2024 - 11:56:45

	- ID: 2
	  Testo: carbonara
	  Data: 26/10/2024 - 12:07:06
```
Cancellazione messaggio con id valido e id invalido
```
@ Server
> delete 2
< Messaggio eliminato
> delete 2
< Messaggio con id 2 non esiste
> end
< Fine ispezione del topic cibo.
```
Esecuzione non consentita di comandi esclusivi ai publisher o ai subscriber
```
@ Client
> send sport
< Devi essere registrato come publisher per inviare questo comando
> listall
< Devi registrarti come publisher o subscriber prima di poter eseguire questo comando
> list
< Devi essere registrato come publisher per inviare questo comando
```
Iscrizione a topic non esistente ed esistente ed elenco topic presenti
```
@ Client
> subscribe cinema
< Il topic inserito non esiste
> show
< Topic presenti:
	  - cibo
> subscribe cibo
< Registrato come subscriber al topic cibo
```
Elenco dei messaggi sul topic a cui il client è iscritto
```
@ Client(publisher)
> listall 
< Messaggi:
	- ID: 1
    Testo: carbonara
	  Data: 26/10/2024 - 12:26:21
```
Invio messaggio su un topic al quale il subscriber è iscritto
```
@ Client(publisher)
> send matriciana
< Inviato messaggio "matriciana"
@ Client(subscriber)
> Nuovo messaggio pubblicato
  ID: 1
  Testo: matriciana
  Data: 26/10/2024 - 12:55:39
```
Arresto client e scollegamento dal server
```
@ Client(publisher)
> quit
< Terminata la connessione al server.
```