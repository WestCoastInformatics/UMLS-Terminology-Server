tsApp.filter('highlight', function($sce) {
  return function(text, phrase) {
    // console.debug("higlight", text, phrase);
    if (text && phrase)
      text = text.replace(new RegExp('(' + phrase.replace(/"/g,'') + ')', 'gi'),
        '<span class="highlighted">$1</span>')
    return $sce.trustAsHtml(text)
  }
})

tsApp.filter('highlightLabelFor', function($sce) {
  return function(text, phrase) {
    // console.debug("higlightLabelFor", text, phrase);
    if (text && phrase)
      text = text.replace(new RegExp('(' + phrase.replace(/"/g,'') + ')', 'gi'),
        '<span style="background-color:#e0ffe0;">$1</span>')
    return $sce.trustAsHtml(text)
  }
})

tsApp.filter('highlightLabel', function($sce) {
  return function(text, phrase) {
    // console.debug("higlightLabel", text, phrase);
    if (text && phrase)
      text = text.replace(new RegExp('(' + phrase.replace(/"/g,'') + ')', 'gi'),
        '<span style="background-color:#e0e0ff;">$1</span>')
    return $sce.trustAsHtml(text)
  }
})