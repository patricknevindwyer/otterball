
# Setup raw SWT App

- [x] Create folder
- [x] Add support libs
- [x] copy default app from Civet
- [x] Add debug window


# Buildable

- [x] Successful build
- [x] Build as app

# Editor Window

- [x] Remove CIVET from menu
- [x] Remove CIVET from loggers
- [x] Basic editor pane
- [x] Styling support (colors, etc)
- [x] Copy/Paste
- [x] Open/Close
- [x] Save
- [x] Clickable menus - works in release, and works after tabbing out in eclipse
- [x] make Debug log non-editable
- [x] Hide error list by default
- [x] Open types of *.json or *

# Base Parsers

- [x] typing / marking parser structure
- [x] Valid JSON typing parser
- [ ] Valid Python dump typing parser (single quotes)
- [ ] unicode escaping format parser (Python)
- [x] typing on load
- [x] multiple objects per file typing
- [x] extract from free text typing
- [x] Editor is saving a ton of NULL BYTES at the end of files

# Extended Parsing

- [ ] Reflow and compact syntax parsers
- [ ] Highlight support

# Easy Edit Features

- [ ] Reflow selection/all
- [ ] Auto surround with matched characters
  - [ ] Double quote
  - [ ] single quote
  - [ ] brackets () 
  - [ ] brackets {} 
  - [ ] brackets [] 
- [ ] Field based search
- [ ] Find/replace
  - [ ] all
  - [ ] field aware
  - [ ] non-syntax based (not syntactically or semantically aware)
- [ ] Condense
- [ ] Select lines by search or select
- [ ] Keep/reject lines
- [ ] To/from base64
- [ ] excise field
- [ ] insert field
- [ ] Indent/Outdent/Reflow selection
- [ ] Detect changes to underlying file

# Analysis

- [ ] Cardinality
- [ ] Type adherence
- [ ] Stream analysis

# Validation

- [ ] Validator structure
- [ ] Extended types validator
- [ ] Invalid values validator
- [ ] Unnecessarily quoted types (int, bool)
- [ ] Duplicate keys


# Future

- [ ] Save as
- [ ] Install/select different theme
- [ ] DebugShell needs to use Theme settings for Font and FontSize
- [ ] Grow/shrink font size (readability/projector mode?)
- [ ] VCS aware (file changes out from under us)