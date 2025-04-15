package net.briclabs.evcoordinator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.briclabs.evcoordinator.ListWithCount;
import net.briclabs.evcoordinator.Logic;
import net.briclabs.evcoordinator.model.SearchRequest;
import org.jooq.DSLContext;
import org.jooq.impl.TableImpl;
import org.jooq.impl.TableRecordImpl;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;

public abstract class ReadController<
        RR extends TableRecordImpl<RR>,
        RP extends Serializable,
        RT extends TableImpl<RR>,
        RL extends Logic<RR, RP, RT>>
        extends ApiController {

    protected final RL readLogic;

    public ReadController(ObjectMapper objectMapper, DSLContext dslContext, RL readLogic) {
        super(objectMapper, dslContext);
        this.readLogic = readLogic;
    }

    /**
     * Retrieves a resource by its unique identifier.
     *
     * @param id the unique identifier of the resource to retrieve.
     * @return a {@code ResponseEntity} containing the resource represented by {@link RP}
     *         if it exists; otherwise, a {@code ResponseEntity} with a status of 404 (Not Found).
     */
    protected ResponseEntity<RP> fetchById(Long id) {
        return readLogic.fetchById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Handles search requests based on the provided search criteria and configuration.
     * It utilizes the search logic to fetch a list of results and the corresponding total count
     * of records matching the given criteria.
     *
     * @param searchRequest the search request containing the search configuration and criteria.
     *                      It specifies filtering, sorting, pagination, and exact match preferences.
     * @return a {@code ResponseEntity} containing a {@link ListWithCount} of type {@link RP}, where the list
     *         represents the search results and the count represents the total number of matching records.
     */
    protected ResponseEntity<ListWithCount<RP>> search(SearchRequest searchRequest) {
        return ResponseEntity.ok(readLogic.fetchByCriteria(
                searchRequest.searchConfiguration().exactMatch(),
                searchRequest.searchCriteria(),
                searchRequest.searchConfiguration().sortColumn(),
                searchRequest.searchConfiguration().sortAsc(),
                searchRequest.searchConfiguration().offset(),
                searchRequest.searchConfiguration().max()
        ));
    }
}
