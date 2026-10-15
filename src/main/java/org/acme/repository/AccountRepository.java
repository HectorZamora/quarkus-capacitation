package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.Account;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AccountRepository implements PanacheRepository<Account> {

    public Optional<Account> findByNumber(String number) {
        return find("number", number).firstResultOptional();
    }

    public List<Account> listByCustomer(Long customerId) {
        return list("customer.id = ?1 order by number", customerId);
    }
}
