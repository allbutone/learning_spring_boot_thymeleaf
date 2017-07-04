package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.boot.actuate.metrics.repository.InMemoryMetricRepository;
import org.springframework.boot.actuate.metrics.writer.Delta;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

/**
 * Created by ren_xt
 */
@Controller
public class HomeController {
    private static final String BASE_PATH = "/imgs";
    private static final String FILENAME = "{filename:.+}";

    private ImageService imageService;
    private CounterService counterService;
    private GaugeService gaugeService;
    private InMemoryMetricRepository inMemoryMetricRepository;

    @Autowired
    public HomeController(ImageService imageService, CounterService counterService, GaugeService gaugeService,
                          InMemoryMetricRepository inMemoryMetricRepository) {
        this.imageService = imageService;
        this.counterService = counterService;
        this.gaugeService = gaugeService;
        this.inMemoryMetricRepository = inMemoryMetricRepository;

        this.counterService.reset("files.uploaded");
        this.gaugeService.submit("files.uploaded.lastBytes", 0);
        this.inMemoryMetricRepository.set(new Metric<Number>("files.uploaded.totalBytes", 0));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/")
    public String index(Model model, Pageable pageable) {
        final Page<Image> page = imageService.findPage(pageable);
        model.addAttribute("page", page);
        if (page.hasPrevious()) {
            model.addAttribute("prev", pageable.previousOrFirst());
        }
        if (page.hasNext()) {
            model.addAttribute("next", pageable.next());
        }
        return "index";
    }

    @RequestMapping(method = RequestMethod.GET, value = BASE_PATH + "/" + FILENAME + "/raw")
    @ResponseBody
    public ResponseEntity<?> oneRawImage(@PathVariable String filename) {
        try {
            Resource resource = imageService.findOneImage(filename);
            return ResponseEntity.ok()//ok() with no arg returns a BodyBuilder
                    .contentLength(resource.contentLength())
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(new InputStreamResource(resource.getInputStream()));
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                    .body("couldn't find " + filename + " ==> " + e.getMessage());
        }
    }

/*
    @RequestMapping(method = RequestMethod.POST, value = BASE_PATH)
    @ResponseBody
    public ResponseEntity<?> createFile(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        try {
            imageService.createImage(file);
            final URI f = new URI(request.getRequestURL().toString() + "/").resolve(file.getOriginalFilename() + "/raw");
            return ResponseEntity.created(f)
                    .body("successfully uploaded " + file.getOriginalFilename());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("failed to upload " + file.getOriginalFilename() + "==> " + e.getMessage());
        } catch (URISyntaxException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }
*/

    @RequestMapping(method = RequestMethod.POST, value = BASE_PATH)
    public String createFile(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            imageService.createImage(file);

            counterService.increment("files.uploaded");
            gaugeService.submit("files.uploaded.lastBytes", file.getSize());
            inMemoryMetricRepository.increment(new Delta<Number>("files.uploaded.totalBytes", file.getSize()));

            redirectAttributes.addFlashAttribute("flash.message", "successfully uploaded file " + file.getOriginalFilename());
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("flash.message", "failed to upload file " + file.getOriginalFilename() + "===>" + e.getMessage());
        }
        return "redirect:/";//flash attribute 在重定向完成后就会消亡
    }

    /*
        @RequestMapping(method = RequestMethod.DELETE, value = BASE_PATH + "/" + FILENAME)
        @ResponseBody
        public ResponseEntity<?> deleteFile(@PathVariable("filename") String filename) {
            try {
                imageService.deleteImage(filename);
                return ResponseEntity.status(HttpStatus.NO_CONTENT)
                        .body("successfully deleted " + filename);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("failed to delete " + filename + "===> " + e.getMessage());
            }
        }
    */
    @RequestMapping(method = RequestMethod.DELETE, value = BASE_PATH + "/" + FILENAME)
    public String deleteFile(@PathVariable("filename") String filename, RedirectAttributes redirectAttributes) {
        try {
            imageService.deleteImage(filename);
            redirectAttributes.addFlashAttribute("flash.message", "successfully deleted file " + filename);
        } catch (IOException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("flash.message", "failed to delete file " + filename + "===>" + e.getMessage());
        }
        return "redirect:/";
    }

}
