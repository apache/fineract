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
            "title": "नम्र निवेदन",
            "body": "नम्र निवेदन है कि अपने सुझाव तथा प्रतिक्रियाओं से भविष्य में हमारा मार्गदर्शन करते रहें। thank you",
            "id": 1
        }
    ],
    tests: [
        {
            what: "find the word %w",
            search: "निवेदन",
            found: 1
        },
        {
            what: "find the word %w",
            search: "thank you",
            found: 1
        },
        {
            what: "never find a word that does not exist, like %w",
            search: "inexistent",
            found: 0
        }
    ]
}