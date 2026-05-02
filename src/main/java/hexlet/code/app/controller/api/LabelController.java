package hexlet.code.app.controller.api;

import hexlet.code.app.dto.IndexDTO;
import hexlet.code.app.dto.LabelCreateDTO;
import hexlet.code.app.dto.LabelDTO;
import hexlet.code.app.dto.LabelUpdateDTO;
import hexlet.code.app.service.LabelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/labels")
@AllArgsConstructor
@Tag(name = "Labels", description = "Operations with labels")
public class LabelController {
    private final LabelService service;

    /**
     * Get a list of all labels.
     * @param dto index data
     * @return list of label DTOs
     */
    @Operation(summary = "Get list of all labels")
    @ApiResponse(responseCode = "200", description = "List of labels",
            headers = {@io.swagger.v3.oas.annotations.headers.Header(
                    name = "X-Total-Count", description = "Total number of labels")})
    @GetMapping
    public ResponseEntity<List<LabelDTO>> index(IndexDTO dto) {
        Page<LabelDTO> labelsPage = this.service.findAll(dto);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(labelsPage.getTotalElements()))
                .body(labelsPage.getContent());
    }

    /**
     * Get label by id.
     * @param id label identifier
     * @return label DTO
     */
    @Operation(summary = "Get label by ID", responses = {
        @ApiResponse(responseCode = "200", description = "Label found"),
        @ApiResponse(responseCode = "404", description = "Label not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<LabelDTO> show(@PathVariable @Parameter(description = "Label ID") Long id) {
        LabelDTO labelDTO = this.service.findById(id);
        return ResponseEntity.ok(labelDTO);
    }

    /**
     * Create a new label.
     * @param dto label creation data
     * @return created label DTO
     */
    @Operation(summary = "Create a new label")
    @ApiResponse(responseCode = "201", description = "Label created")
    @PostMapping
    public ResponseEntity<LabelDTO> create(@RequestBody LabelCreateDTO dto) {
        LabelDTO labelDTO = this.service.create(dto);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(labelDTO.getId())
                .toUri();
        return ResponseEntity.created(location)
                .body(labelDTO);
    }

    /**
     * Update an existing label.
     * @param id label identifier
     * @param dto update data
     * @return updated label DTO
     */
    @Operation(summary = "Update an existing label", responses = {
        @ApiResponse(responseCode = "200", description = "Label updated"),
        @ApiResponse(responseCode = "404", description = "Label not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<LabelDTO> update(@PathVariable @Parameter(description = "Label ID") Long id,
                                          @RequestBody LabelUpdateDTO dto) {
        LabelDTO labelDTO = this.service.update(id, dto);
        return ResponseEntity.ok(labelDTO);
    }

    /**
     * Delete a label by identifier.
     * @param id label identifier
     * @return empty response
     */
    @Operation(summary = "Delete label by ID")
    @ApiResponse(responseCode = "204", description = "Label deleted")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> destroy(@PathVariable @Parameter(description = "Label ID") Long id) {
        this.service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
