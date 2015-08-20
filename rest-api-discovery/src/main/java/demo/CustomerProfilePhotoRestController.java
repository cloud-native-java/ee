package demo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.*;
import java.net.URI;
import java.util.concurrent.Callable;

@RestController
@RequestMapping(value = "/customers/{id}/photo")
public class CustomerProfilePhotoRestController {

    private File root;

    @Autowired
    private CustomerRepository customerRepository;

    private Log log = LogFactory.getLog(getClass());

    @Value("${upload.dir:${user.home}/images}")
    void setUserHome(String uploadDir) {
        this.root = new File(uploadDir );
        Assert.isTrue(this.root.exists() || this.root.mkdirs(),
                String.format("The path '%s' must exist.", this.root.getAbsolutePath()));
    }

    @RequestMapping(method = RequestMethod.GET)
    ResponseEntity<Resource> read(@PathVariable Long id) {
        return this.customerRepository.findById(id)
                .map(customer -> {
                    File file = fileFor(customer);
                    log.info("trying to read file " + file.getAbsolutePath());
                    Assert.isTrue(file.exists(), String.format("file-not-found %s", file.getAbsolutePath()));
                    Resource fileSystemResource = new FileSystemResource(file);
                    return ResponseEntity.ok()
                            .contentType(MediaType.IMAGE_JPEG)
                            .body(fileSystemResource);
                })
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT})
    Callable<ResponseEntity<?>> write(@PathVariable Long id, @RequestParam MultipartFile file) throws Exception {
        log.info(String.format("upload-start /customers/%s/photo (%s bytes)", id, file.getSize()));
        return () ->
                this.customerRepository.findById(id)
                        .map(customer -> {
                            File fileForCustomer = fileFor(customer);
                            try (InputStream in = file.getInputStream();
                                 OutputStream out = new FileOutputStream(fileForCustomer)) {
                                FileCopyUtils.copy(in, out);
                                log.info("wrote to " + fileForCustomer.getAbsolutePath() + " which " +
                                        (fileForCustomer.exists() ? "exists" : "doesn't exist"));

                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                            URI location = ServletUriComponentsBuilder.fromCurrentRequest().buildAndExpand(id).toUri();
                            log.info(String.format("upload-finish /customers/%s/photo (%s)", id, location));
                            return ResponseEntity.created(location).build();
                        })
                        .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    private File fileFor(Customer person) {
        return new File(this.root, Long.toString(person.getId()));
    }
}
