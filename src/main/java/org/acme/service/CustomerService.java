package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.dto.request.CreateCustomerRequest;
import org.acme.dto.response.CustomerResponse;
import org.acme.entity.Customer;
import org.acme.exception.ConflictException;
import org.acme.exception.CustomerNotFoundException;
import org.acme.mapper.BankMapper;
import org.acme.repository.AccountRepository;
import org.acme.repository.CustomerRepository;

import java.util.List;
import java.util.Locale;

@ApplicationScoped
public class CustomerService {

    @Inject
    CustomerRepository customerRepository;

    @Inject
    AccountRepository accountRepository;

    @Inject
    BankMapper mapper;

    @Transactional
    public CustomerResponse create(CreateCustomerRequest request) {
        String document = request.document().trim();
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (customerRepository.findByDocument(document).isPresent()) {
            throw new ConflictException("CUSTOMER_DOCUMENT_ALREADY_EXISTS",
                    "A customer with this document already exists");
        }
        if (customerRepository.findByEmail(email).isPresent()) {
            throw new ConflictException("CUSTOMER_EMAIL_ALREADY_EXISTS",
                    "A customer with this email already exists");
        }

        Customer customer = new Customer();
        customer.name = request.name().trim();
        customer.document = document;
        customer.email = email;
        customerRepository.persist(customer);
        return mapper.toResponse(customer);
    }

    @Transactional
    public CustomerResponse findById(Long id) {
        return customerRepository.findByIdOptional(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    @Transactional
    public List<CustomerResponse> list() {
        return customerRepository.listAll().stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public CustomerResponse update(Long id, CreateCustomerRequest request) {
        Customer customer = customerRepository.findByIdOptional(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        String document = request.document().trim();
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        customerRepository.findByDocument(document).ifPresent(existing -> {
            if (!existing.id.equals(id)) {
                throw new ConflictException("CUSTOMER_DOCUMENT_ALREADY_EXISTS",
                        "A customer with this document already exists");
            }
        });
        customerRepository.findByEmail(email).ifPresent(existing -> {
            if (!existing.id.equals(id)) {
                throw new ConflictException("CUSTOMER_EMAIL_ALREADY_EXISTS",
                        "A customer with this email already exists");
            }
        });

        customer.name = request.name().trim();
        customer.document = document;
        customer.email = email;
        return mapper.toResponse(customer);
    }

    @Transactional
    public void delete(Long id) {
        Customer customer = customerRepository.findByIdOptional(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        if (!accountRepository.listByCustomer(id).isEmpty()) {
            throw new ConflictException("CUSTOMER_HAS_ACCOUNTS",
                    "A customer with accounts cannot be deleted");
        }
        customerRepository.delete(customer);
    }
}
