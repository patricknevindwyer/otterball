
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
- [x] Valid Python dump typing parser (single quotes)
- [x] unicode escaping format parser (Python)
- [x] typing on load
- [x] multiple objects per file typing
- [x] extract from free text typing
- [x] Editor is saving a ton of NULL BYTES at the end of files

# Extended Parsing

- [x] Location aware JSON parser
- [x] JSON compact
- [ ] JSON reflow
- [ ] Highlight support

# Doubly Extended Parsing

- [ ] bareword (JS Style) JSON parser
- [ ] Full parsers for JSON variants
  - [ ] Python Unicode
  - [ ] Single Quoted Strings
  - [ ] JS Style
  
# Easy Edit Features

- [ ] Multiple objects per file aware
- [ ] Reflow selection/all
- [ ] quick toggle reflow/condensed
- [ ] Use parsers for syntax highlighting
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
- [ ] Condense/compact
- [ ] Select lines by search or select
- [ ] Keep/reject lines
- [ ] To/from base64
- [ ] excise field
- [ ] insert field
- [ ] Indent/Outdent/Reflow selection
- [ ] Detect changes to underlying file

# Analysis

- [ ] alternate view pane (analysis pane/sheet)
- [ ] Cardinality
- [ ] Type adherence
- [ ] Stream analysis

# Validation

- [ ] Validator structure
- [ ] Extended types validator (python encoded, date detection)
- [ ] Invalid values validator (geojson, topojson)
- [ ] Unnecessarily quoted types (int, bool)
- [ ] Duplicate keys

# Future

- [ ] Save as
- [ ] DebugShell needs to use Theme settings for Font and FontSize
- [ ] VCS aware (file changes out from under us)
- [ ] Treat root level list as separate objects (easier parsing/hinting)
- [ ] JSONPath analysis transforms
- [ ] breadcrumb for JSONPath to cursor

# Improved Views

- [ ] grow/shrink font
- [ ] improved theme management and view (select sheet)
- [ ] Install/select different theme
- [ ] Grow/shrink font size (readability/projector mode?)

# Bugs

- [ ] Single line file has no line numbers in gutter
- [ ] Lines seem to be missing lines at end when they are new lines
- [ ] fast parsers are not decimal aware (i.e. 9 is fine, 9.1 makes them barf)
- [x] JSON parser doesn't check for delimiters or separators between values (aaaah damn)
- [ ] Parsers are using ArrayLists, should optimize to something faster