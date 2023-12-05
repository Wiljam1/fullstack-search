package se.kth.NRWW;

import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnit;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import org.hibernate.search.mapper.orm.mapping.SearchMapping;
import se.kth.NRWW.model.Book;
import se.kth.NRWW.model.Author;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestQuery;

import io.quarkus.runtime.StartupEvent;
import se.kth.NRWW.model.User;
import se.kth.NRWW.repositories.AuthorRepository;

@Path("/")
public class LibraryResource {

    @Inject
    SearchSession searchSession;

    @Inject
    AuthorRepository authorRepository;

    @Transactional
    void onStart(@Observes StartupEvent ev) throws InterruptedException {
        // only reindex if we imported some content
        //if (Book.count() > 0) {
            searchSession.massIndexer()
                    .startAndWait();
        //}
    }

    @GET
    public List<Author> list() {
        return authorRepository.listAll();
    }

    @GET
    @Path("author/all")
    @Transactional
    public List<Author> allAuthors(@RestQuery Optional<Integer> size) {
        System.out.println("Executing allAuthors..");
        List<Author> result = searchSession.search(Author.class)
                .where(f -> f.matchAll())  // Match all documents
                .sort(f -> f.field("lastName_sort").then().field("firstName_sort"))
                .fetchHits(size.orElse(20));
        System.out.println("Result: " + result);
        return result;
    }

    @PUT
    @Path("book")
    @Transactional
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void addBook(@RestForm String title, @RestForm Long authorId) {
        Author author = authorRepository.findById(authorId);
        if (author == null) {
            return;
        }

        Book book = new Book();
        book.title = title;
        book.author = author;
        //book.persist();

        author.books.add(book);
        authorRepository.persist(author);
    }

//    @DELETE
//    @Path("book/{id}")
//    @Transactional
//    public void deleteBook(Long id) {
//        Book book = Book.findById(id);
//        if (book != null) {
//            book.author.books.remove(book);
//            book.delete();
//        }
//    }

    @PUT
    @Path("author")
    @Transactional
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void addAuthor(@RestForm String firstName, @RestForm String lastName) {
        Author author = new Author();
        author.firstName = firstName;
        author.lastName = lastName;
        authorRepository.persist(author);
    }

    @POST
    @Path("author/{id}")
    @Transactional
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void updateAuthor(Long id, @RestForm String firstName, @RestForm String lastName) {
        Author author = authorRepository.findById(id);
        if (author == null) {
            return;
        }
        author.firstName = firstName;
        author.lastName = lastName;
        authorRepository.persist(author);
    }

    @DELETE
    @Path("author/{id}")
    @Transactional
    public void deleteAuthor(Long id) {
        Author author = authorRepository.findById(id);
        if (author != null) {
            authorRepository.delete(author);
        }
    }

    @GET
    @Path("author/search")
    @Transactional
    public List<Author> searchAuthors(@RestQuery String pattern,
                                      @RestQuery Optional<Integer> size) {
        return searchSession.search(Author.class)
                .where(f -> pattern == null || pattern.trim().isEmpty() ? f.matchAll()
                        : f.simpleQueryString()
                        .fields("firstName", "lastName", "books.title").matching(pattern))
                .sort(f -> f.field("lastName_sort"))//.then().field("firstName_sort"))
                .fetchHits(size.orElse(20));
    }

    @GET
    @Path("users/search")
    @Transactional
    public List<User> searchUsers(@RestQuery String pattern,
                                  @RestQuery Optional<Integer> size) {
        System.out.println("Searching for users...");
        return searchSession.search(User.class)
                .where(f -> pattern == null || pattern.trim().isEmpty() ? f.matchAll()
                        : f.simpleQueryString()
                        .fields("username", "name", "email").matching(pattern))
                .sort(f -> f.field("username_sort"))//.then().field("firstName_sort"))
                .fetchHits(size.orElse(20));
    }
}
