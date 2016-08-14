
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
- [x] JSON reflow

# Doubly Extended Parsing

- [ ] bareword (JS Style) JSON parser
- [ ] Full parsers for JSON variants
  - [ ] Python Unicode
  - [ ] Single Quoted Strings
  - [ ] JS Style
- [ ] Parser needs to return error hints and helpful messages
- [ ] Parser needs to return partial parse tree
- [ ] Parser needs to differentiate between regular strings and object keys
- [ ] Use fast version of parser to build a better _find_sections_ 

# Editor Backend

- [ ] Use parsers for syntax highlighting
- [ ] Editor windows listed in "Window"
- [ ] Save dialog needs *.json extension, not JS
- [ ] Tabs instead of windows?

# Easy Edit - Single Object

- [ ] quick toggle reflow/condensed
- [ ] Auto surround with matched characters
  - [ ] Double quote
  - [ ] single quote
  - [ ] brackets () 
  - [ ] brackets {} 
  - [ ] brackets [] 
- [ ] Indent/Outdent/Reflow selection
- [ ] Condense/compact
- [ ] To/from base64
- [ ] excise field
- [ ] insert field
- [ ] Detect changes to underlying file

# Easy Edit - Multiple Object

- [ ] Editor feature to extract sections
- [ ] Editor feature to map sections to parsed elements
- [ ] Multiple objects per file aware
- [ ] Reflow selection/all
- [ ] Highlight support in JSONParser and friends


# Find/Replace

- [ ] Field based search
- [ ] Find/replace
  - [ ] regex find and highlight
  - [ ] all
  - [ ] field aware
  - [ ] non-syntax based (not syntactically or semantically aware)
- [ ] Select lines by search or select
- [ ] Keep/reject lines

# Bugs/Fixes

- [x] BUG: StyleRanges are off by one...
- [x] Expand highlight theme to be JSON specific

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
- [ ] Make reflow smarter about list length and adding newlines

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