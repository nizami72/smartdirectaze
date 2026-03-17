## unaccent

Шаг 1: Включаем расширение в PostgreSQL
Расширение unaccent уже встроено в Postgres, его нужно просто активировать.
Открой свой pgAdmin (который мы подняли через Docker), выбери базу smartdirect_db, открой Query Tool (инструмент для SQL-запросов) и выполни одну команду:

SQL
CREATE EXTENSION IF NOT EXISTS unaccent;
Проверка: Выполни SELECT unaccent('köynək');. База должна вернуть koynək.

Шаг 2: Решаем проблему специфичных букв (ə и ı)
Тут есть один важный технический нюанс. Расширение unaccent отлично убирает "точки" и "хвостики" (ö->o, ş->s). Но для стандарта Unicode буквы ə (шва) и ı (i без точки) — это самостоятельные символы, а не буквы с акцентом. Поэтому базовый unaccent их не трогает.

Чтобы сделать поиск идеальным, мы обернем unaccent в функцию REPLACE, которая принудительно превратит ə в e, а ı в i.

Шаг 3: Обновляем запрос в Spring Boot
Теперь перепишем твой метод в ProductRepository. Мы используем Native Query, чтобы задействовать мощь PostgreSQL напрямую.

Мы применяем функции нормализации (удаление акцентов + замена ə/ı + нижний регистр) и к тексту в базе, и к запросу пользователя.

Java
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    @Query(value = "SELECT DISTINCT p.* FROM products p " +
           "LEFT JOIN product_titles pt ON p.id = pt.product_id " +
           "WHERE unaccent(REPLACE(REPLACE(LOWER(pt.title_text), 'ə', 'e'), 'ı', 'i')) " +
           "ILIKE unaccent(REPLACE(REPLACE(LOWER(CONCAT('%', TRIM(:query), '%')), 'ə', 'e'), 'ı', 'i')) " +
           "OR unaccent(LOWER(p.sku)) ILIKE unaccent(LOWER(CONCAT('%', TRIM(:query), '%')))", 
           nativeQuery = true)
    List<ProductEntity> searchByKeyword(@Param("query") String query);

}
Как это теперь работает:
В базе лежит: Qara köynək

Клиент пишет боту: qara koynek

Запрос "на лету" преобразует текст из базы в qara koynek и сравнивает с запросом пользователя.

Бинго! Товар найден. ИИ получает данные и отвечает клиенту.

Этот подход спасет тебе огромное количество нервов и сделает бота невероятно "умным" в глазах покупателей. Клиенту не придется напрягаться и писать грамотно.


## Store chatMemoryProvider into database