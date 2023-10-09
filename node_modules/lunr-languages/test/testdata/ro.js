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
            "title": "Romania",
            "body": "România este un stat situat în sud-estul Europei Centrale, pe cursul inferior al Dunării, la nord de peninsula Balcanică și la țărmul nord-vestic al Mării Negre.[8] Pe teritoriul ei este situată aproape toată suprafața Deltei Dunării și partea sudică și centrală a Munților Carpați. Se învecinează cu Bulgaria la sud, Serbia la sud-vest, Ungaria la nord-vest, Ucraina la nord și est și Republica Moldova la est, iar țărmul Mării Negre se găsește la sud-est.",
            "id": 1
        }, {
            "title": "Geografie",
            "body": "Teritoriul actual al României mai este numit și spațiul carpato-danubiano-pontic, deoarece România se suprapune unui sistem teritorial european, conturat după forma cercului Carpaților Românești și a regiunilor limitrofe impuse și subordonate complementar Carpaților, fiind mărginită în partea de sud de fluviul Dunărea, iar în partea de est de Marea Neagră. ocolită",
            "id": 2
        }
    ],
    tests: [
        {
            what: "find the word %w",
            search: "România",
            found: 2
        }, {
            what: "find the word %w",
            search: "stat",
            found: 1
        }, {
            what: "never find a word that does not exist, like %w",
            search: "inexistent",
            found: 0
        }, {
            what: "find a correctly stemmed word %w",
            search: "ocoliţi",
            found: 1
        }
    ]
}