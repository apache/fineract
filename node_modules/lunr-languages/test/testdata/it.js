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
            "title": "Italia",
            "body": "L'Italia (/iˈtalja/[9], ascolta[?·info]), ufficialmente Repubblica Italiana,[10] è una repubblica parlamentare situata nell'Europa meridionale, con una popolazione di 60,6 milioni di abitanti e Roma come capitale. Delimitata dall'arco alpino, confina a nord, da ovest a est, con Francia, Svizzera, Austria e Slovenia; il resto del territorio, circondato dai mari Ligure, Tirreno, Ionio e Adriatico, si protende nel mar Mediterraneo, occupando la penisola italiana e numerose isole (le maggiori sono Sicilia e Sardegna), per un totale di 301 340 km²[11]. Gli Stati della Città del Vaticano e di San Marino sono enclavi della Repubblica.",
            "id": 1
        }, {
            "title": "Suddivisioni amministrative",
            "body": "Gli enti territoriali che, in base all'articolo 114 della Costituzione costituiscono, assieme allo Stato, la Repubblica italiana sono:            le regioni (15 a statuto ordinario e 5 a statuto speciale); le città metropolitane (14); le province e i comuni (rispettivamente 93 e 7 999, dati ISTAT dell'anno 2016).[121] Nell'elenco che segue, per ciascuna regione è riportato lo stemma ufficiale e il nome del capoluogo. pronunziato",
            "id": 2
        }
    ],
    tests: [
        {
            what: "find the word %w",
            search: "Italia*",
            found: 2
        }, {
            what: "find the word %w",
            search: "assieme",
            found: 1
        }, {
            what: "never find a word that does not exist, like %w",
            search: "inexistent",
            found: 0
        }, {
            what: "find a correctly stemmed word %w",
            search: "pronunziarle",
            found: 1
        }
    ]
}