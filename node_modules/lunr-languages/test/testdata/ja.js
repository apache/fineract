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
            "title": "名前",
            "body": "私の名前は中野です",
            "id": 1
        }/*these do not currently work, but I will investigate them later*//*, {
            "title": "日本語全文検索のためのアナライザー",
            "body": "次の設定サンプルは、基本的な日本語検索の為の ja アナライザーと、ja アナライザーにプラスして同義語検索を実現する為の ja_synonym アナライザー、Nグラム検索用の ja_ngram アナライザーを定義しています。\n実際に使用する際には、これらの３つのアナライザーをフィールド毎に使い分けて使用することで、いろいろなシーンで活用できるはずです。",
            "id": 2
        }, {
            "title": "ja_synonym アナライザー (alias : search_analyzer)",
            "body": "ja アナライザーの設定内容に加え、同義語を展開できるアナライザーです。インデックス側または、サーチ側のいずれかのアナライザーで使用することを想定しています。（※ 両方で同じ同義語展開してもあまり意味がないため。）\nインデックス側、サーチ側の両方で同義語展開したい場合は、ja_index_synonym、ja_search_synonym と言うようにそれぞれ定義をわけ、インデックス側の辞書はほとんどメンテナンスしない一般的な同義語を管理し、サーチ側は新語などのコンテンツの内容によって追加変更しそうな同義語を管理するなどしてください。",
            "id": 3
        }*/
    ],
    tests: [
        {
            what: "find the word %w",
            search: "中野",
            found: 1
        }/*these do not currently work, but I will investigate them later*//*, {
            what: "find the word %w",
            search: "アナライザー",
            found: 1
        }, {
            what: "find the word %w",
            search: "analyzer",
            found: 1
        }, {
            what: "find the word %w",
            search: "同義語",
            found: 1
        }*/
    ]
}