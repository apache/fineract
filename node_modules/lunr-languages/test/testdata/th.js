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
            "title": "สวัสดี",
            "body" : "ชาวบ้านในหมู่บ้านกล่าวพร้อมกันว่าสวัสดี",
            "id": 1
        }, {
            "title": "สุภาษิต",
            "body" : "ในน้ำมีปลา ในนามีข้าว แผ่นดินของเราที่แสนอุดมสมบูรณ์",
            "id": 2
        }
    ],
    tests: [
        {
            what: "find the word %w",
            search: "ใน",
            found: 2
        }, {
            what: "find the word %w",
            search: "แผ่นดิน",
            found: 1
        }, {
            what: "never find a word that does not exist, like %w",
            search: "เบื่อ",
            found: 0
        } 
    ]
}
