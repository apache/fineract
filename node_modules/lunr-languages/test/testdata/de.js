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
            "title": "Deutschland",
            "body": "An Deutschland grenzen neun Nachbarländer und naturräumlich im Norden die Gewässer der Nord- und Ostsee, im Süden das Bergland der Alpen. Es liegt in der gemäßigten Klimazone, zählt mit rund 80 Millionen Einwohnern zu den dicht besiedelten Flächenstaaten und gilt international als das Land mit der dritthöchsten Zahl von Einwanderern. aufeinanderfolgenden. auffassen.",
            "id": 1
        }, {
            "title": "Tourismus in Deutschland",
            "body": "Deutschland als Urlaubsziel verfügt über günstige Voraussetzungen: Gebirgslandschaften (Alpen und Mittelgebirge), See- und Flusslandschaften, die Küsten und Inseln der Nord- und Ostsee, zahlreiche Kulturdenkmäler und eine Vielzahl geschichtsträchtiger Städte sowie gut ausgebaute Infrastruktur. Vorteilhaft ist die zentrale Lage in Europa.",
            "id": 2
        }
    ],
    tests: [
        {
            what: "find the word %w",
            search: "Deutsch*",
            found: 2
        }, {
            what: "find the word %w",
            search: "Urlaubsziel*",
            found: 1
        }, {
            what: "never find a word that does not exist, like %w",
            search: "inexistent",
            found: 0
        }, {
            what: "find a correctly stemmed word %w",
            search: "auffassung",
            found: 1
        }
    ]
}