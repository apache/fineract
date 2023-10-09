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
            "title": "España",
            "body": "España, también denominado Reino de España,nota 2 es un país soberano, miembro de la Unión Europea, constituido en Estado social y democrático de derecho y cuya forma de gobierno es la monarquía parlamentaria. Su territorio, con capital en Madrid, está organizado en diecisiete comunidades autónomas y dos ciudades autónomas, formadas estas, a su vez, por cincuenta provincias.",
            "id": 1
        }, {
            "title": "Estado de las autonomías",
            "body": "España es en la actualidad lo que se denomina un «Estado de las autonomías» o «Estado autonómico», un país formalmente unitario que funciona como una federación sui géneris descentralizada de comunidades autónomas, cada una de ellas con diferentes niveles de autogobierno. Las diferencias dentro de este sistema se deben a que el proceso de traspaso de competencias del centro a la periferia fue pensado en un principio como un proceso asimétrico, que garantizase un mayor grado de autogobierno solo a aquellas comunidades que buscaban un tipo de relación más federalista con el resto de España (Andalucía, Cataluña, Galicia, Navarra y País Vasco). Por otro lado, el resto de comunidades autónomas dispondría de un menor autogobierno. A pesar de ello, a medida que fueran pasando los años, otras comunidades como Comunidad Valenciana o Canarias fueran adquiriendo gradualmente más competencias. chicana",
            "id": 2
        }
    ],
    tests: [
        {
            what: "find the word %w",
            search: "España",
            found: 2
        }, {
            what: "find the word %w",
            search: "miembro",
            found: 1
        }, {
            what: "never find a word that does not exist, like %w",
            search: "inexistent",
            found: 0
        }, {
            what: "find a correctly stemmed word %w",
            search: "chicano",
            found: 1
        }
    ]
}