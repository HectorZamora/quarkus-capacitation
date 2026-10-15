package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.Customer;

import java.util.Optional;

@ApplicationScoped
public class CustomerRepository implements PanacheRepository<Customer> {

    public Optional<Customer> findByDocument(String document) {
        return find("document", document).firstResultOptional();
    }

    public Optional<Customer> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }
}
