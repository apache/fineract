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
            "title": "கடவுள் வாழ்த்து",
            "body": "அகர முதல எழுத்தெல்லாம் ஆதி\nபகவன் முதற்றே உலகு thank you",
            "id": 1
        },
        {
            "title": "நீத்தார் பெருமை",
            "body": "சுவைஒளி ஊறுஓசை நாற்றமென ஐந்தின்\nவகைதெரிவான் கட்டே உலகு ",
            "id": 2
        }
        

    ],
    tests: [
        {
            what: "find the word %w",
            search: "அகர",
            found: 1
        },
        {
            what: "find the word %w",
            search: "thank you",
            found: 1
        },
        {
            what: "find the word, like %w",
            search: "கடவுள்",
            found: 1
        },
        {
            what: "find the word, like %w",
            search: "அகர முதல",
            found: 1
        },
        {
            what: "find the word, like %w",
            search: "அகர முதல பகவன் முதற்றே",
            found: 1
        },
        {
            what: "find the word, like %w",
            search: "உலகு",
            found: 2
        },
        {
            what: "never find a word that does not exist, like %w",
            search: "நற்றாள்",
            found: 0
        }
    ]
}