tsApp.filter('highlight', function($sce) {
  return function(text, phrase) {
    if (text && phrase)
      text = text.replace(new RegExp('(' + phrase + ')', 'gi'),
        '<span class="highlighted">$1</span>')
    return $sce.trustAsHtml(text)
  }
})

tsApp.filter('highlightLabelFor', function($sce) {
  return function(text, phrase) {
    if (text && phrase)
      text = text.replace(new RegExp('(' + phrase + ')', 'gi'),
        '<span style="background-color:#e0ffe0;">$1</span>')
    return $sce.trustAsHtml(text)
  }
})

tsApp.filter('highlightLabel', function($sce) {
  return function(text, phrase) {
    if (text && phrase)
      text = text.replace(new RegExp('(' + phrase + ')', 'gi'),
        '<span style="background-color:#e0e0ff;">$1</span>')
    return $sce.trustAsHtml(text)
  }
})