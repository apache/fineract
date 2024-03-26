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
            "title": "Koninkrijk der Nederlanden",
            "body": "Het Koninkrijk der Nederlanden is een soevereine staat samengesteld uit vier landen: Nederland, Aruba, Curaçao en Sint Maarten.[5] Deze landen zijn gelijkwaardige onderdelen van het grondgebied van het Koninkrijk, maar met verschillende staatkundige posities: de Caribische landen zijn door het Statuut voor het Koninkrijk der Nederlanden, sinds 1954 het leidende document voor het Koninkrijk, autonoom, maar beslissen ook mee over door het Statuut benoemde Koninkrijksaangelegenheden, voor zover die aangelegenheden deze drie gebieden raken.",
            "id": 1
        }, {
            "title": "Koninkrijksregering",
            "body": "De regering van het Koninkrijk (Koninkrijksregering) is medewetgever en belast met het bestuur over het Koninkrijk. Over de precieze samenstelling bestaat, bij gebrek aan een regeling in het Statuut en uitspraken hierover in de officiële toelichting, in de literatuur verschil van mening. Sommige schrijvers stellen dat de Koninkrijksregering bestaat uit de Nederlandse regering (Koning en ministers), aangevuld met de gevolmachtigd ministers van Aruba, Curaçao en Sint Maarten. Of, met andere woorden, de Koninkrijksregering bestaat uit de Koning en de Rijksministerraad. Dit naar analogie met de situatie in Nederland, waarin de regering bestaat uit de Koning en de ministerraad.[12] Anderen menen dat de gevolmachtigde ministers weliswaar deel uitmaken van de Rijksministerraad, maar geen (of slechts in oneigenlijke zin) lid zijn van de regering. Zo stelt Borman dat de Koninkrijksregering bestaat uit de Koning en de ministers van het Koninkrijk, dat wil zeggen de ministers van het land Nederland. Hoewel de gevolmachtigd ministers van Aruba, Curaçao en Sint Maarten deel uitmaken van de Rijksministerraad, zijn zij geen minister van het Koninkrijk en maken derhalve geen deel uit van de Koninkrijksregering, aldus Borman. Hij baseert dit onder meer op het feit dat de gevolmachtigde ministers niet politiek verantwoordelijk zijn jegens de Staten-Generaal, en niet ministerieel verantwoordelijk zijn voor het handelen van de Koning. Dit blijkt uit het feit dat de gevolmachtigd minister geen wetgeving contrasigneert.[13] In de praktijk is het verschil in interpretatie niet van belang, omdat over het regeringsbeleid wordt beraadslaagd en beslist in de Rijksministerraad. opglimpende",
            "id": 2
        }
    ],
    tests: [
        {
            what: "find the word %w",
            search: "Nederlanden",
            found: 2
        }, {
            what: "find the word %w",
            search: "bestuur",
            found: 1
        }, {
            what: "never find a word that does not exist, like %w",
            search: "inexistent",
            found: 0
        }, {
            what: "find a correctly stemmed word %w",
            search: "opglimping",
            found: 1
        }
    ]
}