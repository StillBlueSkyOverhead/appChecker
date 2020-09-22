document.addEventListener('DOMContentLoaded', function() {

    var form = document.getElementsByTagName('form')[0];
    var file = document.getElementsByName('file')[0];
    var name = document.getElementsByName('name')[0];

    // bug in kotlinx - no way to set textArea's name
    var src = document.getElementById('src');
    src.setAttribute('name', 'src');

    form.addEventListener('submit', function(e) {

            var noName = isEmpty(name.value);
            var noSrc = isEmpty(src.value);

            if (noName || noSrc) {

                if (noName && !name.classList.contains('is-invalid')) name.classList.add('is-invalid');
                if (noSrc && !src.classList.contains('is-invalid')) src.classList.add('is-invalid');

                // cancel submission
                e.preventDefault();
                e.stopPropagation();

            } else {
                name.classList.remove('is-invalid');
                src.classList.remove('is-invalid');
            }
    });

    file.addEventListener('change', function(e) {
        if (file.value)
            loadAsText(file, src);
    });
});

function openFileDialog() {
    var file = document.getElementsByName('file')[0];
    file.click();
}

function loadAsText(fileElement, srcElement) {

    var reader = new FileReader();

    reader.addEventListener('load', function(e) {
        srcElement.value = e.target.result;
    });

    reader.readAsText(fileElement.files[0], 'utf-8');
    fileElement.value = '';
}

function isEmpty(s) {
    return s == null || !s.trim().length;
}
