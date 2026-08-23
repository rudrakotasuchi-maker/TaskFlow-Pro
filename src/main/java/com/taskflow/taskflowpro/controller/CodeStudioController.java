
package com.taskflow.taskflowpro.controller;

import com.taskflow.taskflowpro.dto.StudioFile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Controller
@RequestMapping("/studio")
public class CodeStudioController {

    // Open Studio
    @GetMapping("/{projectId}")
    public String studio(@PathVariable Long projectId,
                         Model model) {

        model.addAttribute("projectId", projectId);

        return "studio";
    }

    // Save file
    @PostMapping("/save")
    @ResponseBody
    public String saveFile(@RequestBody StudioFile studioFile)
            throws IOException {

        File folder = new File("workspace");

        if (!folder.exists()) {
            folder.mkdirs();
        }

        File file = new File(folder, studioFile.getFileName());

        FileWriter writer = new FileWriter(file);

        writer.write(studioFile.getContent());

        writer.close();

        return "Saved Successfully!";
    }
}

