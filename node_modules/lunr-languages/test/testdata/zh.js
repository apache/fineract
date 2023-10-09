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
            "title": "【新闻1+1】Mercedes Benz女车主维权 车主到底难在何处？",
            "body": "你们说给我3天时间，我给了你3天，我给你们5个3天，15天，15天你不给我个方案，最后给我的方案是换发动机，15天最后给我这个方案，您觉得我接受得了吗？",
            "id": 1
        }, {
            "title": "央视CCTV13新闻1+1正在说奔驰漏油事件",
            "body": "我这个车没开出去这个门发动机漏油，你给我说讲三包，给我车主说免费换发动机，我跟你说要求你说可以退款可以换车，你又最后说换发动机，还给我说打12315，大哥你觉得合适吗？",
            "id": 2
        }, {
            "title": "奔驰女车主接受专访，经销商咋就这么强势？",
            "body": "我打电话给奔驰金融，我说我这天15天没开过，我可不可以暂停还款，人家说不可以，这是用你个人征信做的贷款，你贷款必须还，我车没开到，我凭什么还这个贷款。",
            "id": 3
        }
    ],
    tests: [
        {
            what: "find the word %w",
            search: "车主",
            found: 3
        }, {
            what: "find the word %w",
            search: "Benz",
            found: 1
        }, {
            what: "find the word %w",
            search: "Mercedes Benz",
            found: 1
        }, {
            what: "find the word %w",
            search: "12315",
            found: 1
        }, {
            what: "find the word %w",
            search: "CCTV13",
            found: 1
        }, {
            what: "never find a word that does not exist, like %w",
            search: "美女",
            found: 0
        }, {
            what: "never find a character like %w",
            search: "，",
            found: 0
        }
    ]
}