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
            "title": "Cô gái Bạc Liêu hội ngộ gia đình sau 22 năm lưu lạc tại Trung Quốc",
            "body": "Một cô gái mất tích 22 năm trước, đã bị gia đình khai tử nay được tìm thấy ở cửa khẩu biên giới tỉnh Lạng Sơn giáp Trung Quốc và đã được ân nhân giúp đỡ để hội ngộ cùng gia đình.",
            "id": 1
        }, {
            "title": "Tiếng Việt",
            "body": "gia đình",
            "id": 2
        }, 
    ],
    tests: [
        {
            what: "find the word %w",
            search: "một",
            found: 1
        }, {
            what: "find the word %w",
            search: "năm trước",
            found: 1
        }, {
            what: "never find a word that does not exist, like %w",
            search: "sáu",
            found: 0
        }, {
            what: "find the word %w",
            search: "đình",
            found: 2
         }, {
            what: "do not find the stop word %w",
            search: "là",
            found: 0
        }
    ]
}
