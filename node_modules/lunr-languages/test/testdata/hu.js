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
            "title": "Magyarország",
            "body": "Magyarország közép-európai ország a Kárpát-medencében, amelyet északról Szlovákia, északkeletről Ukrajna, keletről és délkeletről Románia, délről Szerbia és Horvátország, délnyugatról Szlovénia, nyugatról pedig Ausztria határol. Fővárosa és egyben legnépesebb városa Budapest. Az ország többek között az Európai Unió, a NATO, az OECD és az ENSZ tagja is, része a schengeni övezetnek, valamint egyik alapítója az úgynevezett Visegrádi Együttműködés szervezetnek. Hivatalos nyelve a magyar.",
            "id": 1
        }, {
            "title": "Honfoglalás és a Magyar Királyság",
            "body": "Az ősmagyarság legkorábbi ismert hazájának a Volga vidékét tekinthetjük (Magna Hungaria). Ezt követően a 7. és a 9. század között a Don folyó melletti Etelközben éltek. Kisebb részük keleten maradt és a volgai bolgárokhoz csatlakozott a Volgai Bolgárországban. Vannak nyomai egy Kaukázusi Magyarországnak is, ennek mibenléte azonban a források elégtelensége miatt vitatott. A nép nagyobb része nyugatra vonult. 895-ben, a honfoglalás során az egész Kárpát-medencét birtokukba vették. A Képes Krónika szerint ez volt a magyarok második bejövetele, Atilla hun király halála után száz évvel, Kr. u. 677-ben[22] (az eltérésre a Kettős honfoglalás, illetve a Kitalált középkor elmélet adhat magyarázatot). A 10. század első felében kalandozó hadjárataikkal rémületben tartották Nyugat- és Dél-Európát, ekkoriban a keresztény templomokban a könyörgés részévé váltak[23] a félelmet tükröző szavak, mint a 900-as évek elején született Modenai himnuszban: „Ab Ungerorum nos defendas iaculis”, azaz „Védj meg minket a magyarok támadásától”[24] A komolyabb Nyugat-Európába induló hadjáratok az augsburgi csata (955) után véget értek, de Dél-Európa felé még tovább is folytak a kalandozások. A kalandozások tényleges végét csak a 973-as quedlinburgi találkozó jelentette. babgulyás",
            "id": 2
        }
    ],
    tests: [
        {
            what: "find the word %w",
            search: "Magyarország",
            found: 2
        }, {
            what: "find the word %w",
            search: "tekinthetjük",
            found: 1
        }, {
            what: "never find a word that does not exist, like %w",
            search: "inexistent",
            found: 0
        }, {
            what: "find a correctly stemmed word %w",
            search: "babgulyást",
            found: 1
        }
    ]
}