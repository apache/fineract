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
            "title": "Türkiye",
            "body": "Türkiye ya da resmî adıyla Türkiye Cumhuriyeti, topraklarının büyük bölümü Anadolu'ya, küçük bir bölümü ise Balkanlar'ın uzantısı olan Trakya'ya yayılmış bir ülke. Kuzeybatıda Bulgaristan, batıda Yunanistan, kuzeydoğuda Gürcistan, doğuda Ermenistan, İran ve Azerbaycan'ın ekslav toprağı Nahçıvan, güneydoğuda ise Irak ve Suriye komşusudur. Güneyini Akdeniz, batısını Ege Denizi ve kuzeyini Karadeniz çevreler. Marmara Denizi ise İstanbul Boğazı ve Çanakkale Boğazı ile birlikte Anadolu'yu Trakya'dan yani Asya'yı Avrupa'dan ayırır. Türkiye, Avrupa ve Asya'nın kavşak noktasında yer alması sayesinde önemli bir jeostratejik güce sahiptir.[7]",
            "id": 1
        }, {
            "title": "Coğrafya",
            "body": "Türkiye, iki kıtada toprağı bulunan bir Avrasya ülkesidir.[163] Topraklarının %97'si Asya üzerinde bulunur ve bu kısım Anadolu diye adlandırılır. Kalan %3'lük kısım ise Avrupa kıtasında kalır ve Doğu Trakya diye adlandırılır. Marmara Denizi, Çanakkale ve İstanbul Boğazı Anadolu'yu Trakya'dan, Asya'yı Avrupa'dan ayırır.[164][165]",
            "id": 2
        }
    ],
    tests: [
        {
            what: "find the word %w",
            search: "Türkiye",
            found: 2
        }, {
            what: "find the word %w",
            search: "kıtada",
            found: 1
        }, {
            what: "never find a word that does not exist, like %w",
            search: "inexistent",
            found: 0
        }
    ]
}