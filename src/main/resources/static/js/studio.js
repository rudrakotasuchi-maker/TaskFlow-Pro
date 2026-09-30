
// =====================================================
// TASKFLOW STUDIO
// studio.js
// Monaco Editor + File Explorer + File Management
// Terminal + Resizers
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
    let autoSaveTimer = null;


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
    // UPDATE CURRENT FILE IN MEMORY
    // =================================================

    function updateCurrentFileInMemory() {

        if (currentFileId === null) {
            return;
        }

        const currentFile =
            files.find(function (file) {

                return String(file.id) ===
                    String(currentFileId);

            });

        if (!currentFile) {
            return;
        }

        currentFile.content =
            window.monacoEditor.getValue();
    }


    // =================================================
    // AUTO SAVE
    // =================================================

    window.monacoEditor.onDidChangeModelContent(
        function () {

            if (!currentFileId) {
                return;
            }

            updateCurrentFileInMemory();

            clearTimeout(autoSaveTimer);

            autoSaveTimer = setTimeout(
                function () {

                    saveCurrentFile(false);

                },
                1000
            );

        }
    );


    // =================================================
    // OPEN FILE
    // =================================================

    function openFile(file) {

        if (!file) {
            return;
        }

        console.log(
            "Opening file:",
            file.fileName
        );

        currentFileId =
            file.id;

        window.monacoEditor.setValue(
            file.content || ""
        );

        const language =
            getLanguage(file.fileName);

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
            document.getElementById(
                "fileExplorer"
            );

        if (!explorer) {

            console.error(
                "fileExplorer element not found"
            );

            return;
        }

        explorer.innerHTML = "";


        // =================================================
        // FILE LIST
        // =================================================

        files.forEach(function (file) {

            const fileElement =
                document.createElement("div");

            fileElement.className =
                "file-item";

            fileElement.dataset.id =
                file.id;

            fileElement.innerHTML =
                "📄 " + file.fileName;


            fileElement.addEventListener(
                "click",
                function () {

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


            explorer.appendChild(
                fileElement
            );

        });


        // =================================================
        // SEPARATOR
        // =================================================

        const separator =
            document.createElement("hr");

        explorer.appendChild(
            separator
        );


        // =================================================
        // NEW FILE BUTTON
        // =================================================

        const newFileBtn =
            document.createElement("button");

        newFileBtn.id =
            "newFileBtn";

        newFileBtn.className =
            "btn btn-success w-100 mb-2";

        newFileBtn.innerText =
            "+ New File";

        explorer.appendChild(
            newFileBtn
        );


        // =================================================
        // RENAME BUTTON
        // =================================================

        const renameFileBtn =
            document.createElement("button");

        renameFileBtn.id =
            "renameFileBtn";

        renameFileBtn.className =
            "btn btn-warning w-100 mb-2";

        renameFileBtn.innerText =
            "✏ Rename File";

        explorer.appendChild(
            renameFileBtn
        );


        // =================================================
        // DELETE BUTTON
        // =================================================

        const deleteFileBtn =
            document.createElement("button");

        deleteFileBtn.id =
            "deleteFileBtn";

        deleteFileBtn.className =
            "btn btn-danger w-100";

        deleteFileBtn.innerText =
            "🗑 Delete File";

        explorer.appendChild(
            deleteFileBtn
        );


        // =================================================
        // EVENTS
        // =================================================

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
    // SAVE CURRENT FILE
    // =================================================

    function saveCurrentFile(
        showAlert = true
    ) {

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

                alert(
                    "Current file not found."
                );

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
        document.getElementById(
            "saveBtn"
        );


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
        document.getElementById(
            "runBtn"
        );


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


        const inputElement =
            document.getElementById(
                "consoleInput"
            );


        const programInput =
            inputElement
                ? inputElement.value
                : "";


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


                const isError =
                    output.includes(
                        "❌ Compilation Error"
                    ) ||
                    output.includes(
                        "❌ Runtime Error"
                    ) ||
                    output.includes(
                        "error:"
                    );


                const statusText =
                    isError
                        ? "✗ Program Execution Failed"
                        : "✓ Program Executed Successfully";


                const consolePanel =
                    document.getElementById(
                        "consolePanel"
                    );


                if (consolePanel) {

                    consolePanel.innerText =

                        `==========================================
TASKFLOW TERMINAL
==========================================

▶ Running ${currentFile.language} Program...

${output}

------------------------------------------
    ${statusText}

Execution Time : ${endTime - startTime} ms

==========================================`;

                    consolePanel.scrollTop =
                        consolePanel.scrollHeight;

                }


                runBtn.disabled = false;

                runBtn.innerText =
                    "▶ Run";

            })

            .catch(function (error) {

                console.error(
                    "Run error:",
                    error
                );


                const consolePanel =
                    document.getElementById(
                        "consolePanel"
                    );


                if (consolePanel) {

                    consolePanel.innerText =

                        `==========================================
TASKFLOW TERMINAL
==========================================

❌ Program Execution Failed

${error.message}

==========================================`;

                }


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
                "FILES RECEIVED:",
                data
            );


            files =
                data.map(
                    function (file) {

                        return {

                            id:
                                file.id,

                            fileName:
                                file.fileName,

                            language:
                                file.language,

                            content:
                                file.content || ""

                        };

                    }
                );


            // =================================================
            // NO FILES
            // =================================================

            if (files.length === 0) {

                files.push({

                    id:
                        "temporary-main",

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


            renderExplorer();


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


            const explorer =
                document.getElementById(
                    "fileExplorer"
                );


            if (explorer) {

                explorer.innerHTML =

                    `<div style="color:#ff6666;">
                        ❌ Failed to load files.
<br><br>
    ${error.message}
</div>`;

    }

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
                    );


                if (!input) {
                    return;
                }


                const value =
                    input.value;


                if (value.trim() === "") {
                    return;
                }


                const consolePanel =
                    document.getElementById(
                        "consolePanel"
                    );


                if (consolePanel) {

                    consolePanel.innerText +=
                        value + "\n";

                }


                input.value = "";

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
        getLanguage(
        fileName
        );


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

        openFile(file);


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
        typeof currentFile.id ===
        "string"
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


    console.log(
    "TaskFlow Studio initialization completed."
    );

    });


    // =====================================================
    // TASKFLOW TERMINAL RESIZER
    // =====================================================

    (function setupTerminalResizer() {

        const developmentArea =
        document.querySelector(
        ".development-area"
        );

        const terminalArea =
        document.getElementById(
        "terminalArea"
        );

        const resizer =
        document.getElementById(
        "terminalResizer"
        );


        if (
        !developmentArea ||
        !terminalArea ||
        !resizer
        ) {

        console.warn(
        "TaskFlow terminal resizer elements not found."
        );

        return;
    }


        let isResizing = false;


        // =================================================
        // START
        // =================================================

        resizer.addEventListener(
        "pointerdown",
        function (event) {

        isResizing = true;

        resizer.setPointerCapture(
        event.pointerId
        );

        document.body.classList.add(
        "terminal-resizing"
        );

        event.preventDefault();

    }
        );


        // =================================================
        // DRAG
        // =================================================

        resizer.addEventListener(
        "pointermove",
        function (event) {

        if (!isResizing) {
        return;
    }


        const rect =
        developmentArea.getBoundingClientRect();


        let terminalHeight =
        rect.bottom -
        event.clientY;


        const MIN_TERMINAL_HEIGHT =
        140;


        const MAX_TERMINAL_HEIGHT =
        rect.height * 0.75;


        terminalHeight =
        Math.max(
        MIN_TERMINAL_HEIGHT,
        Math.min(
        terminalHeight,
        MAX_TERMINAL_HEIGHT
        )
        );


        developmentArea.style.setProperty(
        "--terminal-height",
        terminalHeight + "px"
        );


        // IMPORTANT:
        // Your Monaco editor is stored as
        // window.monacoEditor

        if (
        window.monacoEditor &&
        typeof window.monacoEditor.layout ===
        "function"
        ) {

        requestAnimationFrame(
        function () {

        window.monacoEditor.layout();

    }
        );

    }

    }
        );


        // =================================================
        // STOP
        // =================================================

        function stopResizing(event) {

        if (!isResizing) {
        return;
    }


        isResizing = false;


        document.body.classList.remove(
        "terminal-resizing"
        );


        try {

        resizer.releasePointerCapture(
        event.pointerId
        );

    } catch (error) {
        // Ignore
    }

    }


        resizer.addEventListener(
        "pointerup",
        stopResizing
        );


        resizer.addEventListener(
        "pointercancel",
        stopResizing
        );


        // =================================================
        // DOUBLE CLICK RESET
        // =================================================

        resizer.addEventListener(
        "dblclick",
        function () {

        developmentArea.style.setProperty(
        "--terminal-height",
        "270px"
        );


        if (
        window.monacoEditor &&
        typeof window.monacoEditor.layout ===
        "function"
        ) {

        requestAnimationFrame(
        function () {

        window.monacoEditor.layout();

    }
        );

    }

    }
        );


        // =================================================
        // CLEAR TERMINAL
        // =================================================

        const clearTerminalBtn =
        document.getElementById(
        "clearTerminalBtn"
        );


        if (clearTerminalBtn) {

        clearTerminalBtn.addEventListener(
        "click",
        function () {

        const consolePanel =
        document.getElementById(
        "consolePanel"
        );


        if (consolePanel) {

        consolePanel.innerHTML = `

                        <div class="terminal-welcome">

                            <div class="terminal-brand">
                                TASKFLOW TERMINAL
                            </div>

                            <div class="terminal-status">
                                ● Ready
                            </div>

                            <div class="terminal-line">
                                Terminal cleared.
                            </div>

                        </div>

                    `;

    }

    }
        );

    }


        // =================================================
        // MAXIMIZE TERMINAL
        // =================================================

        const maximizeTerminalBtn =
        document.getElementById(
        "maximizeTerminalBtn"
        );


        if (maximizeTerminalBtn) {

        maximizeTerminalBtn.addEventListener(
        "click",
        function () {

        developmentArea.classList.toggle(
        "terminal-maximized"
        );


        if (
        developmentArea.classList.contains(
        "terminal-maximized"
        )
        ) {

        maximizeTerminalBtn.innerText =
        "🗗";

    } else {

        maximizeTerminalBtn.innerText =
        "⛶";

    }


        if (
        window.monacoEditor &&
        typeof window.monacoEditor.layout ===
        "function"
        ) {

        setTimeout(
        function () {

        window.monacoEditor.layout();

    },
        50
        );

    }

    }
        );

    }


        console.log(
        "TaskFlow Terminal Resizer loaded successfully."
        );


        // =====================================================
        // TASKFLOW SIDE PANEL RESIZERS
        // =====================================================

        (function setupSidePanelResizers() {

        const mainLayout =
        document.querySelector(
        ".main-layout"
        );

        const sidebar =
        document.querySelector(
        ".sidebar"
        );

        const aiPanel =
        document.querySelector(
        ".ai-panel"
        );

        const explorerResizer =
        document.getElementById(
        "explorerResizer"
        );

        const aiResizer =
        document.getElementById(
        "aiResizer"
        );


        if (
        !mainLayout ||
        !sidebar ||
        !aiPanel ||
        !explorerResizer ||
        !aiResizer
        ) {

        console.warn(
        "TaskFlow side panel resizers not found."
        );

        return;
    }


        // =================================================
        // EXPLORER WIDTH
        // =================================================

        function setExplorerWidth(width) {

        const MIN_WIDTH =
        170;

        const MAX_WIDTH =
        420;


        width =
        Math.max(
        MIN_WIDTH,
        Math.min(
        width,
        MAX_WIDTH
        )
        );


        mainLayout.style.setProperty(
        "--explorer-width",
        width + "px"
        );

    }


        // =================================================
        // AI WIDTH
        // =================================================

        function setAIWidth(width) {

        const MIN_WIDTH =
        260;

        const MAX_WIDTH =
        500;


        width =
        Math.max(
        MIN_WIDTH,
        Math.min(
        width,
        MAX_WIDTH
        )
        );


        mainLayout.style.setProperty(
        "--ai-width",
        width + "px"
        );

    }


        // =================================================
        // EXPLORER RESIZER
        // =================================================

        let resizingExplorer =
        false;


        explorerResizer.addEventListener(
        "pointerdown",
        function (event) {

        resizingExplorer =
        true;


        explorerResizer.setPointerCapture(
        event.pointerId
        );


        document.body.classList.add(
        "side-resizing"
        );


        event.preventDefault();

    }
        );


        explorerResizer.addEventListener(
        "pointermove",
        function (event) {

        if (!resizingExplorer) {
        return;
    }


        const layoutRect =
        mainLayout.getBoundingClientRect();


        const newWidth =
        event.clientX -
        layoutRect.left;


        setExplorerWidth(
        newWidth
        );

    }
        );


        function stopExplorerResize(
        event
        ) {

        if (!resizingExplorer) {
        return;
    }


        resizingExplorer =
        false;


        document.body.classList.remove(
        "side-resizing"
        );


        try {

        explorerResizer.releasePointerCapture(
        event.pointerId
        );

    } catch (error) {
        // Ignore
    }

    }


        explorerResizer.addEventListener(
        "pointerup",
        stopExplorerResize
        );


        explorerResizer.addEventListener(
        "pointercancel",
        stopExplorerResize
        );


        // =================================================
        // AI PANEL RESIZER
        // =================================================

        let resizingAI =
        false;


        aiResizer.addEventListener(
        "pointerdown",
        function (event) {

        resizingAI =
        true;


        aiResizer.setPointerCapture(
        event.pointerId
        );


        document.body.classList.add(
        "side-resizing"
        );


        event.preventDefault();

    }
        );


        aiResizer.addEventListener(
        "pointermove",
        function (event) {

        if (!resizingAI) {
        return;
    }


        const layoutRect =
        mainLayout.getBoundingClientRect();


        const newWidth =
        layoutRect.right -
        event.clientX;


        setAIWidth(
        newWidth
        );

    }
        );


        function stopAIResize(
        event
        ) {

        if (!resizingAI) {
        return;
    }


        resizingAI =
        false;


        document.body.classList.remove(
        "side-resizing"
        );


        try {

        aiResizer.releasePointerCapture(
        event.pointerId
        );

    } catch (error) {
        // Ignore
    }

    }


        aiResizer.addEventListener(
        "pointerup",
        stopAIResize
        );


        aiResizer.addEventListener(
        "pointercancel",
        stopAIResize
        );


        console.log(
        "TaskFlow side panel resizers loaded."
        );

    })();

    })();

