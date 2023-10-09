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
            "title": "Sverige",
            "body": "Sverige /ˈsværjɛ/ (officiellt Konungariket Sverige (info)) är ett nordiskt land på Skandinaviska halvön i Nordeuropa. Sverige har landgräns i väst med Norge, i nordost med Finland samt angränsande territorialvatten till Danmark i sydväst och Finland i öst. Landet har kuster mot Bottenviken, Bottenhavet, Ålands hav, Östersjön, Öresund, Kattegatt och Skagerrak.",
            "id": 1
        }, {
            "title": "Demografi",
            "body": "Sverige uppnådde 10 miljoner invånare den 20 januari 2017 enligt Statistiska centralbyrån[54]. Befolkningen ökar med 60–80 000 personer per år varav tre fjärdedelar beror på invandringsöverskottet. Landet har en folktäthet på 21,9 invånare per kvadratkilometer. Det är därmed det land med det 84:e högsta invånarantalet, men med endast den 152:a högsta befolkningstätheten. Befolkningstätheten är i allmänhet större i de södra delarna i Sverige. Såsom exempel kan nämnas att det i landskapet Lappland bor 100 980 invånare; Lapplands yta är 109 702 km²; i Lunds kommun bor det fler, 102 257 invånare; Lunds kommun har en yta på endast 442,87 km². klokare",
            "id": 2
        }
    ],
    tests: [
        {
            what: "find the word %w",
            search: "Sverige",
            found: 2
        }, {
            what: "find the word %w",
            search: "miljoner",
            found: 1
        }, {
            what: "never find a word that does not exist, like %w",
            search: "inexistent",
            found: 0
        }, {
            what: "find a correctly stemmed word %w",
            search: "klokast",
            found: 1
        }
    ]
}