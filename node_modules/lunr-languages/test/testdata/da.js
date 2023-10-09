module.exports = {
    fields: [
        {
            name: 'title',
            config: { boost: 10 }
        }, {
            name: 'body'
        }
    ],
    documents: [
        {
            "title": "Danmark",
            "body": "Danmark (fra oldnordisk: Danernes mark) er et land i Skandinavien beliggende i det nordlige Europa. Danmark danner sammen med Grønland og Færøerne Det Danske Rige også kaldet Kongeriget Danmark. Danmark grænser til Tyskland mod syd og er omgivet af flere have: Vesterhavet (Nordsøen), Skagerrak og Kattegat på vest-, nord- og østsiden af Jylland, Kattegat og Østersøen nord og syd for de danske øer. Danmark består af halvøen Jylland og 443 navngivne øer, småøer og holme.",
            "id": 1
        }, {
            "title": "Infrastruktur",
            "body": "Danmarks infrastruktur er veludbygget og generelt i god stand[157] og består hovedsageligt af almindelige landeveje, motorveje og et jernbanenet med to primære togselskaber, Arriva og DSB. Danmark er blandt de lande i verden med flest kilometer motorvej pr. indbygger. Lastbiler er oftest den del af transportkæden, der leverer godset til den endelige destination, og dominerer derfor den danske godstransport. Den står også for ca. 80 % af den samlede godstransport, skibe og færger udgør ca. 19 %, mens jernbanen står for kun ca. 1 % af den indenlandske godstransport.[158] Flere end 170 millioner passagerer transporteres årligt på jernbanenettet.[159] Der findes fem internationale lufthavne i Danmark, hvor Kastrup Lufthavn med sine knap 21 millioner passagerer årligt er Skandinaviens største lufthavn.[160] Den fungerer som lufthavn ikke bare for danskere, men også for store dele af den sydsvenske befolkning. Andre store lufthavne er Billund i Jylland og Aalborg, Esbjerg og Aarhus. undertrykkerens",
            "id": 2
        }
    ],
    tests: [
        {
            what: "find the word %w",
            search: "Danmark*",
            found: 2
        }, {
            what: "find the word %w",
            search: "infrastruktur*",
            found: 1
        }, {
            what: "never find a word that does not exist, like %w",
            search: "inexistent",
            found: 0
        }, {
            what: "find a correctly stemmed word %w",
            search: "undertrykkeres",
            found: 1
        }
    ]
}