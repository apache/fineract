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
            "title": "Portugal",
            "body": "Portugal, oficialmente República Portuguesa,[9][nota 9] é um país soberano[nota 10] unitário localizado no sudoeste da Europa, cujo território se situa na zona ocidental da Península Ibérica e em arquipélagos no Atlântico Norte. O território português tem uma área total de 92 090 km²,[10] sendo delimitado a norte e leste por Espanha e a sul e oeste pelo oceano Atlântico, compreendendo uma parte continental e duas regiões autónomas: os arquipélagos dos Açores e da Madeira. Portugal é a nação mais a ocidente do continente europeu. O nome do país provém da sua segunda maior cidade, Porto, cujo nome latino-celta era Portus Cale.[11][12]",
            "id": 1
        }, {
            "title": "Demografia",
            "body": "A população portuguesa é composta por 16,4 % com idade compreendida entre os 0 e os 14 anos, 66,2 % entre os 15 e os 64 anos e 17,4 % com mais de 65 anos, como tal, a população tem vindo a envelhecer. A esperança média de vida é de 78,04 anos. Em termos de alfabetização, 93,3 % sabem ler e escrever, tendo a taxa de analfabetismo vindo a descer ao longo dos anos.[82] O crescimento populacional situa-se nos 0,305 %, nascendo 10,45 por cada mil habitantes e falecendo 10,62 por cada mil habitantes, o que faz com que a população esteja a ser renovada, contribuindo para este facto a taxa de fertilidade que se situa nos 1,32 em 2010.[83] Portugal é um dos países com mais baixa taxa de mortalidade infantil abaixo dos 5 anos (3,7 por mil em 2010) no mundo.[84] quilométricas",
            "id": 2
        }
    ],
    tests: [
        {
            what: "find the word %w",
            search: "Portugal",
            found: 2
        }, {
            what: "find the word %w",
            search: "populacional",
            found: 1
        }, {
            what: "never find a word that does not exist, like %w",
            search: "inexistent",
            found: 0
        }, {
            what: "find a correctly stemmed word %w",
            search: "quilométricos",
            found: 1
        }
    ]
}