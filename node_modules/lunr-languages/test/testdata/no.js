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
            "title": "Norge",
            "body": "Kongeriket Norge (nynorsk: Kongeriket Noreg, nordsamisk: Norgga gonagasriika) er et nordisk, europeisk land og en selvstendig stat vest på Den skandinaviske halvøy. Landet er langt og smalt, og kysten strekker seg langs Nord-Atlanteren, hvor også Norges kjente fjorder befinner seg. Totalt dekker det relativt tynt befolkede landet 385 000 kvadratkilometer med litt over fem millioner innbyggere (2016).",
            "id": 1
        }, {
            "title": "Demografi",
            "body": "Norges folketall passerte 5 millioner i mars 2012, den første millionen ble passert i 1822.[18] Kjønnsfordelingen var ved inngangen til 2012 på 50,1 % menn og 49,9 % kvinner.[19] Dette var første gang det er mannsoverskudd i Norge siden det ble gjort folketelling fordelt på kjønn første gang i 1769. Aldersfordelingen er 25 % fra 0 til 20 år, 62 % fra 20 til 66 år, og 13 % fra 66 år og oppover.[20] Omkring 34 % av landets befolkning bor i de fire små Oslofjord-fylkene Akershus, Østfold, Vestfold og Oslo, som kun dekker 3,6 % av landets areal.[21] opparbeidelse",
            "id": 2
        }
    ],
    tests: [
        {
            what: "find the word %w",
            search: "Norge",
            found: 2
        }, {
            what: "find the word %w",
            search: "dekker",
            found: 2
        }, {
            what: "never find a word that does not exist, like %w",
            search: "inexistent",
            found: 0
        }, {
            what: "find a correctly stemmed word %w",
            search: "opparbeider",
            found: 1
        }
    ]
}