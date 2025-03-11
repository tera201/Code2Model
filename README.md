# Code-to-Model Library

**Code-to-Model** is a library for analyzing source code and extracting its structure into an abstract model stored in SQLite for efficient reuse. The caching mechanism ensures that previously analyzed files are skipped, significantly improving performance.

## Features
- **Source Code Analysis**: Parses source code to extract class structures, relationships, and method signatures.
- **Abstract Model Generation**: Represents code structure in a database-friendly format for further using.
- **SQLite Storage**: Saves UML models into a database for fast retrieval and incremental analysis.
- **Incremental Processing**: Avoids reanalyzing files that have already been processed.
- **Performance Optimization**: Speeds up analysis by leveraging stored models.

## License
This project is licensed under the **Apache 2.0 License**. See the `LICENSE` file for details.

## Credits
Developed by [Roman Naryshkin](https://github.com/tera201).

