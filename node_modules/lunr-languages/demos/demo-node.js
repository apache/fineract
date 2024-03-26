var lunr = require('../test/lunr/lunr-2.0.0-alpha.5.js');
require('../lunr.stemmer.support.js')(lunr);
require('../lunr.de.js')(lunr);

/* init lunr */
var idx = lunr(function () {
    // use the language (de)
    this.use(lunr.de);
    // then, the normal lunr index initialization
    this.field('title', { boost: 10 })
    this.field('body')

    /* add documents to index */
    this.add({
        "title": "Deutschland",
        "body": "An Deutschland grenzen neun Nachbarländer und naturräumlich im Norden die Gewässer der Nord- und Ostsee, im Süden das Bergland der Alpen. Es liegt in der gemäßigten Klimazone, zählt mit rund 80 Millionen Einwohnern zu den dicht besiedelten Flächenstaaten und gilt international als das Land mit der dritthöchsten Zahl von Einwanderern.",
        "id": 1
    });
    this.add({
        "title": "Tourismus in Deutschland",
        "body": "Deutschland als Urlaubsziel verfügt über günstige Voraussetzungen: Gebirgslandschaften (Alpen und Mittelgebirge), See- und Flusslandschaften, die Küsten und Inseln der Nord- und Ostsee, zahlreiche Kulturdenkmäler und eine Vielzahl geschichtsträchtiger Städte sowie gut ausgebaute Infrastruktur. Vorteilhaft ist die zentrale Lage in Europa.",
        "id": 2
    });
});

console.log('Search for `Deutsch`: ', idx.search('Deutsch*'));
console.log('Search for `Urlaubsziel`: ', idx.search('Urlaubsziel*'));
console.log('Search for `inexistent`: ', idx.search('inexistent'));