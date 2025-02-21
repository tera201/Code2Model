# Code-to-UML Library

**Code-to-UML** is a library for analyzing source code, converting it into **Eclipse UML2 models**, and storing the extracted information in **SQLite** for efficient reuse. The caching mechanism ensures that previously analyzed files are skipped, significantly improving performance.

## Features
- **Source Code Analysis**: Parses source code to extract class structures, relationships, and method signatures.
- **Eclipse UML2 Integration**: Converts parsed code into **UML models** using the Eclipse UML2 framework.
- **SQLite Storage**: Saves UML models into a database for fast retrieval and incremental analysis.
- **Incremental Processing**: Avoids reanalyzing files that have already been processed.
- **Performance Optimization**: Speeds up UML generation by leveraging stored models.

## License
This project is licensed under the **Apache 2.0 License**. See the `LICENSE` file for details.

## Credits
Developed by [Roman Naryshkin](https://github.com/tera201).

