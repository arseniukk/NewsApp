package com.example.newsapp

import com.example.newsapp.network.ArticleDto
import com.example.newsapp.network.SourceDto
import com.example.newsapp.rss.RssItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MappersTest {

    // --- ТЕСТИ ФОРМАТУВАННЯ ДАТИ ---

    @Test
    fun `formatDate returns correct format for valid ISO date`() {
        val rawDate = "2025-10-15T14:30:00Z"
        val formattedDate = formatDate(rawDate) // Викликаємо formatDate напряму
        assertEquals("15.10.2025", formattedDate)
    }

    @Test
    fun `formatDate handles invalid date string gracefully`() {
        val invalidDate = "Not a date"
        val result = formatDate(invalidDate)
        assertEquals("Not a date", result) // Має повернути оригінальний рядок
    }

    // --- ТЕСТИ DTO (NewsAPI) ---

    @Test
    fun `ArticleDto_toArticleEntity maps fields correctly`() {
        val dto = ArticleDto(
            title = "Test Title",
            description = "Test Desc",
            author = "Author", // У нас є автор
            url = "http://url.com",
            urlToImage = "http://img.com",
            publishedAt = "2025-10-15T10:00:00Z",
            source = SourceDto(id = "bbc", name = "BBC News"), // І є джерело
            content = "Content"
        )

        val entity = dto.toArticleEntity("Technology")

        assertEquals("Test Title", entity?.title)
        assertEquals("Technology", entity?.category)
        // ВИПРАВЛЕНО: Очікуємо "Author", бо це поле має пріоритет над джерелом в вашому коді
        assertEquals("Author", entity?.author)
        assertEquals("15.10.2025", entity?.date)
        assertEquals("http://url.com".hashCode(), entity?.id)
    }

    // +++ НОВИЙ ТЕСТ: Перевірка логіки, коли автора немає +++
    @Test
    fun `toArticleEntity uses source name when author is missing`() {
        val dto = ArticleDto(
            title = "Title", description = "Desc",
            author = null, // Автора немає
            source = SourceDto(id = "bbc", name = "BBC News"), // Має взяти це
            url = "url", urlToImage = null, publishedAt = null, content = null
        )

        val entity = dto.toArticleEntity("General")

        assertEquals("BBC News", entity?.author)
    }

    @Test
    fun `ArticleDto_toArticleEntity returns null when required fields are missing`() {
        val dtoWithoutTitle = ArticleDto(title = null, description = "Desc", url = "url", author = null, source = null, urlToImage = null, publishedAt = null, content = null)
        assertNull(dtoWithoutTitle.toArticleEntity("General"))

        val dtoWithoutUrl = ArticleDto(title = "Title", description = "Desc", url = null, author = null, source = null, urlToImage = null, publishedAt = null, content = null)
        assertNull(dtoWithoutUrl.toArticleEntity("General"))
    }

    // --- ТЕСТИ RSS (XML) ---

    @Test
    fun `RssItem_toArticle cleans HTML tags from description`() {
        val rssItem = RssItem(
            title = "RSS Title",
            description = "<p>This is <b>bold</b> text.</p><br/>",
            link = "http://rss.com",
            pubDate = "Fri, 13 Oct 2025 10:00:00 +0300",
            author = "RSS Author"
        )

        val article = rssItem.toArticle()

        // Перевіряємо, чи зникли теги <p>, <b>, <br/>
        assertEquals("This is bold text.", article?.description?.trim())
        assertEquals("RSS Author", article?.author)
        // Перевіряємо форматування дати для RSS
        assertEquals("13.10.2025", article?.date)
    }

    @Test
    fun `RssItem_toArticle uses default author if missing`() {
        val rssItem = RssItem(
            title = "Title",
            description = "Desc",
            link = "link",
            pubDate = null,
            author = null // Автор відсутній
        )

        val article = rssItem.toArticle()

        assertEquals("Українська правда", article?.author) // Має підставити дефолтне значення
    }

    // --- ТЕСТИ ENTITY (База даних) ---

    @Test
    fun `ArticleEntity_toArticle maps correctly`() {
        val entity = ArticleEntity(
            id = 123,
            title = "DB Title",
            description = "DB Desc",
            author = "DB Author",
            date = "01.01.2025",
            category = "Sports",
            imageUrl = "img"
        )

        val article = entity.toArticle()

        assertEquals(123, article.id)
        assertEquals("DB Title", article.title)
        assertEquals("Sports", article.category)
    }
}