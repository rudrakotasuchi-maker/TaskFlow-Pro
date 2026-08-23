// =====================================================
// TASKFLOW STUDIO
// studio.js
// Monaco Editor + File Explorer + File Management
// =====================================================

require.config({
    paths: {
        vs: "https://cdn.jsdelivr.net/npm/monaco-editor@0.52.2/min/vs"
    }
});

require(["vs/editor/editor.main"], function () {

    console.log("====================================");
    console.log("TaskFlow Studio JavaScript Loaded");
    console.log("====================================");

    // =================================================
    // GLOBAL VARIABLES
    // =================================================

    window.monacoEditor = null;

    let files = [];
    let currentFileId = null;


    // =================================================
    // CREATE MONACO EDITOR
    // =================================================

    window.monacoEditor = monaco.editor.create(
        document.getElementById("editor"),
        {
            value: "",
            language: "java",
            theme: "vs-dark",
            automaticLayout: true,
            minimap: {
                enabled: false
            },
            fontSize: 14,
            lineNumbers: "on",
            wordWrap: "on"
        }
    );

    console.log("Monaco editor created");
    // =================================================
// AUTO SAVE EDITOR CONTENT
// =================================================

    let autoSaveTimer = null;

    window.monacoEditor.onDidChangeModelContent(function () {

        if (!currentFileId) {
            return;
        }

        const current = files.find(
            file => file.id == currentFileId
        );

        if (!current) {
            return;
        }

        // Update current file content
        current.content = window.monacoEditor.getValue();

        // Clear previous auto-save timer
        clearTimeout(autoSaveTimer);

        // Save automatically 1 second after typing stops
        autoSaveTimer = setTimeout(function () {

            saveCurrentFile(false);

            console.log(
                "✅ Auto-saved:",
                current.fileName
            );

        }, 1000);

    });


    // =================================================
    // LANGUAGE DETECTOR
    // =================================================

    function getLanguage(fileName) {

        if (!fileName) {
            return "plaintext";
        }

        const extension =
            fileName.split(".").pop().toLowerCase();

        switch (extension) {

            case "java":
                return "java";

            case "py":
                return "python";

            case "cpp":
                return "cpp";

            case "c":
                return "c";

            case "js":
                return "javascript";

            case "html":
                return "html";

            case "css":
                return "css";

            case "json":
                return "json";

            case "xml":
                return "xml";

            case "sql":
                return "sql";

            default:
                return "plaintext";
        }
    }


    // =================================================
    // OPEN FILE
    // =================================================

    function openFile(file) {

        if (!file) {
            return;
        }

        console.log("Opening file:", file.fileName);

        currentFileId = file.id;

        window.monacoEditor.setValue(
            file.content || ""
        );

        const language = getLanguage(file.fileName);

        monaco.editor.setModelLanguage(
            window.monacoEditor.getModel(),
            language
        );

        window.monacoEditor.focus();

        console.log(
            "Current file:",
            file.fileName,
            "Language:",
            language
        );
    }


    // =================================================
    // RENDER FILE EXPLORER
    // =================================================

    function renderExplorer() {

        const explorer =
            document.getElementById("fileExplorer");

        if (!explorer) {
            console.error(
                "fileExplorer element not found"
            );
            return;
        }

        explorer.innerHTML = "";


        // ---------------------------------------------
        // FILE LIST
        // ---------------------------------------------

        files.forEach(function (file) {

            const fileElement =
                document.createElement("div");

            fileElement.className = "file-item";

            fileElement.dataset.id = file.id;

            fileElement.innerHTML =
                "📄 " + file.fileName;

            fileElement.addEventListener(
                "click",
                function () {

                    // Save currently opened file first
                    if (currentFileId !== null) {
                        updateCurrentFileInMemory();
                    }

                    const selectedId =
                        this.dataset.id;

                    const selectedFile =
                        files.find(
                            function (f) {
                                return String(f.id) ===
                                    String(selectedId);
                            }
                        );

                    if (!selectedFile) {
                        console.error(
                            "File not found:",
                            selectedId
                        );
                        return;
                    }

                    openFile(selectedFile);
                }
            );

            explorer.appendChild(fileElement);

        });


        // ---------------------------------------------
        // SEPARATOR
        // ---------------------------------------------

        const separator =
            document.createElement("hr");

        explorer.appendChild(separator);


        // ---------------------------------------------
        // NEW FILE BUTTON
        // ---------------------------------------------

        const newFileBtn =
            document.createElement("button");

        newFileBtn.id = "newFileBtn";

        newFileBtn.className =
            "btn btn-success w-100 mb-2";

        newFileBtn.innerText =
            "+ New File";

        explorer.appendChild(newFileBtn);


        // ---------------------------------------------
        // RENAME BUTTON
        // ---------------------------------------------

        const renameFileBtn =
            document.createElement("button");

        renameFileBtn.id =
            "renameFileBtn";

        renameFileBtn.className =
            "btn btn-warning w-100 mb-2";

        renameFileBtn.innerText =
            "✏ Rename File";

        explorer.appendChild(renameFileBtn);


        // ---------------------------------------------
        // DELETE BUTTON
        // ---------------------------------------------

        const deleteFileBtn =
            document.createElement("button");

        deleteFileBtn.id =
            "deleteFileBtn";

        deleteFileBtn.className =
            "btn btn-danger w-100";

        deleteFileBtn.innerText =
            "🗑 Delete File";

        explorer.appendChild(deleteFileBtn);


        // ---------------------------------------------
        // BUTTON EVENTS
        // ---------------------------------------------

        newFileBtn.addEventListener(
            "click",
            createNewFile
        );

        renameFileBtn.addEventListener(
            "click",
            renameCurrentFile
        );

        deleteFileBtn.addEventListener(
            "click",
            deleteCurrentFile
        );
    }


    // =================================================
    // UPDATE CURRENT FILE IN MEMORY
    // =================================================

    function updateCurrentFileInMemory() {

        if (currentFileId === null) {
            return;
        }

        const currentFile =
            files.find(
                function (file) {
                    return String(file.id) ===
                        String(currentFileId);
                }
            );

        if (!currentFile) {
            return;
        }

        currentFile.content =
            window.monacoEditor.getValue();
    }


    // =================================================
    // MONACO CONTENT CHANGE
    // =================================================

    window.monacoEditor.onDidChangeModelContent(
        function () {

            updateCurrentFileInMemory();

        }
    );


    // =================================================
    // SAVE CURRENT FILE
    // =================================================

    function saveCurrentFile(showAlert = true) {

        if (currentFileId === null) {

            if (showAlert) {
                alert(
                    "Please select a file first."
                );
            }

            return;
        }

        const currentFile =
            files.find(
                function (file) {
                    return String(file.id) ===
                        String(currentFileId);
                }
            );

        if (!currentFile) {

            if (showAlert) {
                alert("Current file not found.");
            }

            return;
        }


        // Get latest editor content

        currentFile.content =
            window.monacoEditor.getValue();


        console.log(
            "Saving file:",
            currentFile.fileName
        );


        fetch("/workspace/save", {

            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify({

                id: currentFile.id,

                fileName: currentFile.fileName,

                content: currentFile.content,

                language: currentFile.language,

                project: {
                    id: projectId
                }

            })

        })

            .then(function (response) {

                if (!response.ok) {

                    throw new Error(
                        "Save failed: HTTP " +
                        response.status
                    );
                }

                return response.json();

            })

            .then(function (savedFile) {

                console.log(
                    "File saved:",
                    savedFile
                );


                // Update ID returned from database

                currentFile.id =
                    savedFile.id;

                currentFileId =
                    savedFile.id;


                if (showAlert) {

                    alert(
                        "✅ File Saved Successfully!"
                    );

                }

            })

            .catch(function (error) {

                console.error(
                    "Save error:",
                    error
                );

                if (showAlert) {

                    alert(
                        "❌ Save Failed!\n\n" +
                        error.message
                    );

                }

            });
    }


    // =================================================
    // SAVE BUTTON
    // =================================================

    const saveBtn =
        document.getElementById("saveBtn");

    if (saveBtn) {

        saveBtn.addEventListener(
            "click",
            function () {

                saveCurrentFile(true);

            }
        );

    }


    // =================================================
    // RUN BUTTON
    // =================================================

    const runBtn =
        document.getElementById("runBtn");

    if (runBtn) {

        runBtn.addEventListener(
            "click",
            function () {

                runProgram();

            }
        );

    }


    // =================================================
    // RUN PROGRAM
    // =================================================

    function runProgram() {

        if (currentFileId === null) {

            alert(
                "Please select a file first."
            );

            return;
        }


        const currentFile =
            files.find(
                function (file) {
                    return String(file.id) ===
                        String(currentFileId);
                }
            );


        if (!currentFile) {

            alert(
                "Current file not found."
            );

            return;
        }


        currentFile.content =
            window.monacoEditor.getValue();


        const programInput =
            document.getElementById(
                "consoleInput"
            ).value;


        console.log(
            "Language:",
            currentFile.language
        );

        console.log(
            "Program Input:",
            programInput
        );

        console.log(
            "Current File:",
            currentFile.fileName
        );

        console.log(
            "Source Code:"
        );

        console.log(
            currentFile.content
        );


        runBtn.disabled = true;

        runBtn.innerText =
            "Running...";


        const startTime =
            Date.now();


        fetch(
            "/run?language=" +
            encodeURIComponent(
                currentFile.language
            ) +
            "&input=" +
            encodeURIComponent(
                programInput
            ),
            {

                method: "POST",

                headers: {
                    "Content-Type":
                        "text/plain"
                },

                body:
                currentFile.content

            }
        )

            .then(function (response) {

                return response.text()
                    .then(function (output) {

                        if (!response.ok) {

                            throw new Error(
                                output ||
                                "Program execution failed."
                            );

                        }

                        return output;

                    });

            })

            .then(function (output) {

                const endTime =
                    Date.now();

                let isError =
                    output.includes("❌ Compilation Error") ||
                    output.includes("❌ Runtime Error") ||
                    output.includes("error:");

                let statusText =
                    isError
                        ? "✗ Program Execution Failed"
                        : "✓ Program Executed Successfully";

                document.getElementById(
                    "consolePanel"
                ).innerText =

                    `==========================================
TASKFLOW TERMINAL
==========================================

▶ Running ${currentFile.language} Program...

${output}

------------------------------------------
${statusText}

Execution Time : ${endTime - startTime} ms

==========================================`;

                runBtn.disabled = false;

                runBtn.innerText =
                    "▶ Run";

            });

    }


    // =================================================
    // LOAD FILES FROM DATABASE
    // =================================================

    console.log(
        "Loading files for project:",
        projectId
    );


    fetch(
        "/workspace/project/" +
        projectId
    )

        .then(function (response) {

            console.log(
                "Workspace response status:",
                response.status
            );


            if (!response.ok) {

                throw new Error(
                    "Could not load workspace files. HTTP " +
                    response.status
                );

            }


            return response.json();

        })

        .then(function (data) {

            console.log(
                "FILES RECEIVED FROM BACKEND:",
                data
            );


            // ---------------------------------------------
            // Convert database response to local objects
            // ---------------------------------------------

            files = data.map(
                function (file) {

                    return {

                        id: file.id,

                        fileName:
                        file.fileName,

                        language:
                        file.language,

                        content:
                            file.content || ""

                    };

                }
            );


            console.log(
                "FILES ARRAY:",
                files
            );


            // ---------------------------------------------
            // NO FILES IN DATABASE
            // ---------------------------------------------

            if (files.length === 0) {

                console.log(
                    "No files found in database."
                );


                // Create temporary default file
                // It will appear in Explorer but
                // is NOT automatically saved to DB.

                files.push({

                    id: "temporary-main",

                    fileName:
                        "Main.java",

                    language:
                        "java",

                    content:
                        `public class Main {

    public static void main(String[] args) {

        System.out.println("Welcome To TaskFlowPro!");

    }

}`

                });

            }


            // ---------------------------------------------
            // SHOW FILES
            // ---------------------------------------------

            renderExplorer();


            // ---------------------------------------------
            // OPEN FIRST FILE
            // ---------------------------------------------

            if (files.length > 0) {

                openFile(
                    files[0]
                );

            }

        })

        .catch(function (error) {

            console.error(
                "ERROR LOADING FILES:",
                error
            );


            document.getElementById(
                "fileExplorer"
            ).innerHTML =

                `<div style="color:#ff6666;">
                ❌ Failed to load files.
                <br><br>
                ${error.message}
            </div>`;

        });


    // =================================================
    // SEND PROGRAM INPUT
    // =================================================

    const sendInputBtn =
        document.getElementById(
            "sendInputBtn"
        );


    if (sendInputBtn) {

        sendInputBtn.addEventListener(
            "click",
            function () {

                const input =
                    document.getElementById(
                        "consoleInput"
                    ).value;


                if (input.trim() === "") {
                    return;
                }


                document.getElementById(
                    "consolePanel"
                ).innerText +=
                    input + "\n";


                document.getElementById(
                    "consoleInput"
                ).value = "";

            }
        );

    }


    // =================================================
    // CREATE NEW FILE
    // =================================================

    function createNewFile() {

        const fileName =
            prompt(
                "Enter file name:",
                "Example.java"
            );


        if (!fileName ||
            fileName.trim() === "") {

            return;
        }


        const language =
            getLanguage(fileName);


        fetch(
            "/workspace/create",
            {

                method: "POST",

                headers: {

                    "Content-Type":
                        "application/json"

                },

                body: JSON.stringify({

                    fileName:
                        fileName.trim(),

                    language:
                    language,

                    content:
                        "",

                    project: {

                        id:
                        projectId

                    }

                })

            }
        )

            .then(function (response) {

                if (!response.ok) {

                    throw new Error(
                        "File creation failed. HTTP " +
                        response.status
                    );

                }

                return response.json();

            })

            .then(function (file) {

                console.log(
                    "Created file:",
                    file
                );


                files.push({

                    id:
                    file.id,

                    fileName:
                    file.fileName,

                    language:
                    file.language,

                    content:
                        file.content || ""

                });


                renderExplorer();

                openFile(
                    file
                );


                alert(
                    "✅ File Created Successfully!"
                );

            })

            .catch(function (error) {

                console.error(
                    "Create file error:",
                    error
                );


                alert(
                    "❌ File Creation Failed!\n\n" +
                    error.message
                );

            });

    }


    // =================================================
    // RENAME FILE
    // =================================================

    function renameCurrentFile() {

        if (currentFileId === null) {

            alert(
                "Please select a file first."
            );

            return;
        }


        const currentFile =
            files.find(
                function (file) {

                    return String(file.id) ===
                        String(currentFileId);

                }
            );


        if (!currentFile) {

            return;
        }


        const newFileName =
            prompt(
                "Enter new file name:",
                currentFile.fileName
            );


        if (!newFileName ||
            newFileName.trim() === "") {

            return;
        }


        currentFile.fileName =
            newFileName.trim();


        currentFile.language =
            getLanguage(
                currentFile.fileName
            );


        currentFile.content =
            window.monacoEditor.getValue();


        fetch(
            "/workspace/save",
            {

                method: "POST",

                headers: {

                    "Content-Type":
                        "application/json"

                },

                body: JSON.stringify({

                    id:
                    currentFile.id,

                    fileName:
                    currentFile.fileName,

                    content:
                    currentFile.content,

                    language:
                    currentFile.language,

                    project: {

                        id:
                        projectId

                    }

                })

            }
        )

            .then(function (response) {

                if (!response.ok) {

                    throw new Error(
                        "Rename failed."
                    );

                }

                return response.json();

            })

            .then(function (updatedFile) {

                currentFile.id =
                    updatedFile.id;

                currentFileId =
                    updatedFile.id;


                renderExplorer();

                openFile(
                    currentFile
                );


                alert(
                    "✅ File Renamed Successfully!"
                );

            })

            .catch(function (error) {

                console.error(
                    "Rename error:",
                    error
                );


                alert(
                    "❌ Rename Failed!\n\n" +
                    error.message
                );

            });

    }


    // =================================================
    // DELETE FILE
    // =================================================

    function deleteCurrentFile() {

        if (currentFileId === null) {

            alert(
                "Please select a file."
            );

            return;
        }


        const currentFile =
            files.find(
                function (file) {

                    return String(file.id) ===
                        String(currentFileId);

                }
            );


        if (!currentFile) {

            return;
        }


        // Temporary file

        if (
            typeof currentFile.id === "string"
        ) {

            files =
                files.filter(
                    function (file) {

                        return file !==
                            currentFile;

                    }
                );


            currentFileId =
                null;


            renderExplorer();


            window.monacoEditor.setValue(
                ""
            );


            return;
        }


        const confirmed =
            confirm(
                "Are you sure you want to delete " +
                currentFile.fileName +
                "?"
            );


        if (!confirmed) {

            return;
        }


        fetch(
            "/workspace/" +
            currentFile.id,
            {

                method:
                    "DELETE"

            }
        )

            .then(function (response) {

                if (!response.ok) {

                    throw new Error(
                        "Delete failed. HTTP " +
                        response.status
                    );

                }

            })

            .then(function () {

                files =
                    files.filter(
                        function (file) {

                            return String(file.id) !==
                                String(currentFile.id);

                        }
                    );


                currentFileId =
                    null;


                renderExplorer();


                if (files.length > 0) {

                    openFile(
                        files[0]
                    );

                } else {

                    window.monacoEditor.setValue(
                        ""
                    );

                }


                alert(
                    "✅ File Deleted Successfully!"
                );

            })

            .catch(function (error) {

                console.error(
                    "Delete error:",
                    error
                );


                alert(
                    "❌ Delete Failed!\n\n" +
                    error.message
                );

            });

    }


    // =================================================
    // AI SUPPORT
    // =================================================

    // IMPORTANT:
    // AIController should use:
    //
    // window.monacoEditor.getValue()
    //
    // Do NOT use:
    //
    // editor.getValue()
    //
    // because this file does not create a global
    // variable called editor.


    console.log(
        "TaskFlow Studio initialization completed."
    );

});