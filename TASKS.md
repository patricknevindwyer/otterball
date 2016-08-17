
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
- [ ] parsing needs to return indication whether a parse completely validated, or was a partial parse
- [ ] Parser needs to return error hints and helpful messages
- [ ] Parser needs to return partial parse tree
- [ ] Parser needs to differentiate between regular strings and object keys
- [ ] Use fast version of parser to build a better _find_sections_ 

# Editor Backend

- [ ] Use multiple parsers for syntax highlighting
- [ ] Editor windows listed in "Window"
- [ ] Save dialog needs *.json extension, not JS
- [ ] Tabs instead of windows?
- [ ] Update JSONDocument in JSONShell only after a typing delay, not on every edit

# Easy Edit - Single Object

- [x] quick toggle reflow/condensed
- [x] reflow/condensed toggle needs to maintain relative cursor positioning
- [x] !! Lookahead in the parsers will overflow the string boundary trying to read larger values.
- [x] Auto surround with matched characters
  - [x] Double quote
  - [x] single quote
  - [x] brackets {} 
  - [x] brackets [] 
- [x] /Reflow selection
- [ ] Indent/Outdent
- [x] Fix reflow of lists
- [x] Condense/compact
- [ ] To/from base64
- [ ] excise field
- [ ] insert field
- [x] Edit menu
  - [x] Expand
  - [x] Compact
- [ ] Detect changes to underlying file
- [x] Ability to select next containing block (select my surroundings)
- [x] Reflow only selection
- [ ] Copy/Paste in menu
- [ ] Add version string on startup

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

# Performance

- [ ] Profile opening / flowing / parsing large samples
- [ ] Benchmark file read/write strategies
- [ ] Trim down bundled app

# Future

- [ ] Reopen documents at start
- [ ] Save unsaved documents in temp
- [ ] Save as
- [ ] DebugShell needs to use Theme settings for Font and FontSize
- [ ] VCS aware (file changes out from under us)
- [ ] Treat root level list as separate objects (easier parsing/hinting)
- [ ] JSONPath analysis transforms
- [ ] breadcrumb for JSONPath to cursor
- [ ] Make reflow smarter about list length and adding newlines
- [ ] Slim down the .app release target
- [ ] Select enclosing block of selection, not just from caret position

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
- [x] BUG: StyleRanges are off by one...
- [x] Expand highlight theme to be JSON specific
- [ ] Reflow/condense currently replaces entire contents of editor
- [ ] surrounding a multiline block with automatching (object brackets, for instance) overruns the gutter
- [ ] Block select from EOF crashes
- [ ] Block select => reflow => select => reflow crashes and mangles the document
- [ ] square brackets add a newline when reflowing when they don't contain any elements. they shouldn't do this, they should act like curly brackets.

# Uncategorized

- use java NIO streams to truly stream data?
- better timing/tracing of performance during parse cycle
- restarting parser when encountering errors
- add an error type or a skipped type to the ElementType
- word wrap
- key bindings (VIM style or EMACS style)
- alternative themes
- filter/clear debug terminal
- smaller gutter numbers
- status bar ala - TextMate
- select line
- maybe a thin line between gutter and text area?
- informational header area as well as footer?
- JSONPath in header?
- What about a title bar icon with DND like a native mac app?
- preferences system
  - theme
  - fonts
  - default parser
  - indent characters (tab, vs spaces)
  - key bindings
- make all of the helpers optional in pref system
- lines
  - goto
  - select
  - extract to new doc
  - compare
- long section eliding, i.e. replace a long string with "..."
  - would require a very different data model
  - would be a good way to leverage JSONDocument
- Save/Quit dialog needs to have key shortcuts (ctrl-d for no)    

# Speeding up via Async

The biggest thing we could do right now is make a better way to detect a text area that needs async update from various plugins in response to a typing lull. Like:

- ModifyEvent -> OBEditor
- OBEditor -> Detect last change time
- OBEditor -> lull > 250ms && dirty
- OBEditor -> JSONShell update async services
- OBEditor -> Block/interrupt changes when typing starts again

Use a semaphored object to make sure we can properly interrupt events in the async queue?

How do we register other events that should calculate in async, like the brace matcher?

How do we protect the text from getting mangled by events stomping each other?

Style events can get interrupted.

TextModification events cannot get interrupted.

Maybe a queue/map of what to call async, each with a mode/type?