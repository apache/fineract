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
            "title": "Suomi",
            "body": "Suomen tasavalta eli Suomi (ruots. Republiken Finland, Finland) on valtio Pohjois-Euroopassa Itämeren rannalla. Suomi kuuluu Pohjoismaihin ja Euroopan unioniin. Se rajautuu idässä Venäjään ja siihen kuuluvaan Karjalan tasavaltaan, pohjoisessa Norjaan ja lännessä Ruotsiin. Etelässä lähin maa on Viro Suomenlahden eteläpuolella. Suomen pääkaupunki ja suurin kaupunki on Helsinki. Ahvenanmaan maakunnalla on itsehallinto, ja lähes koko maakunnan alue on demilitarisoitu.",
            "id": 1
        }, {
            "title": "Suomen suuriruhtinaskunta",
            "body": "Venäjän tappio Friedlandin taistelussa Itä-Preussissa 14.6.1807 pakotti Aleksanteri I:n neuvottelemaan Ranskan Napoleon Bonaparten kanssa. 7.7.1807 tehdyssä Tilsitin sopimuksessa Venäjä suostui rauhan ehtona pakottamaan Ruotsin ja Tanskan mukaan Ranskan julistamaan Iso-Britannian mannermaasulkemus-nimellä tunnettuun kauppasaartoon. Ruotsin kuningas ei tähän suostunut ja venäläiset joukot saapuivat helmikuussa 1808 Suomeen, mutta ankaran talven takia varsinaiset taistelut olivat Suomen sodassa vähäisiä. Venäläisten valtaamasta suomalaisalueesta muodostettiin Suomen suuriruhtinaskunta vuonna 1809 Porvoon valtiopäivillä. Käsite Suomen suuriruhtinaskunta oli esiintynyt jo Ruotsin vallan aikana, mutta varsinaista hallinnollista ja valtiollista merkitystä se alkoi saada vasta nyt. innostuksella",
            "id": 2
        }
    ],
    tests: [
        {
            what: "find the word %w",
            search: "Suomen",
            found: 2
        }, {
            what: "find the word %w",
            search: "maakunnalla",
            found: 1
        }, {
            what: "never find a word that does not exist, like %w",
            search: "inexistent",
            found: 0
        }, {
            what: "find a correctly stemmed word %w",
            search: "innostuksensa",
            found: 1
        }
    ]
}