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
            "title": "France",
            "body": "La France Prononciation du titre dans sa version originale Écouter, officiellement la République française Prononciation du titre dans sa version originale Écouter, est un État transcontinental souverain, dont le territoire métropolitain est situé en Europe de l'Ouest. Ce dernier a des frontières terrestres avec la Belgique, le Luxembourg, l'Allemagne, la Suisse, l'Italie, l'Espagne et les principautés d'Andorre et de MonacoN 6,6 et dispose d'importantes façades maritimes dans l'Atlantique, la Manche, la mer du Nord et la Méditerranée. Son territoire ultramarin s'étend dans les océans Indien7, Atlantique8 et Pacifique9 ainsi que sur le continent sud-américain10 et a des frontières terrestres avec le Brésil, le Suriname et le Royaume des Pays-Bas.",
            "id": 1
        }, {
            "title": "Politique et administration",
            "body": "La France est une démocratie libérale, dont le gouvernement a la forme d’une république. Les fondements de l’organisation politique et administrative actuelle de la France ont été fixés en 1958 par la Constitution de la Cinquième République. Selon l’article premier de cette constitution, « la France est une République indivisible, laïque, démocratique et sociale ». Depuis 2003, ce même article affirme en outre que « son organisation est décentralisée continuelle",
            "id": 2
        }
    ],
    tests: [
        {
            what: "find the word %w",
            search: "France",
            found: 2
        }, {
            what: "find the word %w",
            search: "gouvernement",
            found: 1
        }, {
            what: "never find a word that does not exist, like %w",
            search: "inexistent",
            found: 0
        }, {
            what: "find a correctly stemmed word %w",
            search: "continuellement",
            found: 1
        }
    ]
}