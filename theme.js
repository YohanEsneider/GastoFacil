document.addEventListener('DOMContentLoaded', () => {
  const themeToggleButtons = document.querySelectorAll('.btn-theme-toggle');
  
  function updateToggleIcon(button, theme) {
    const icon = button.querySelector('i');
    if (!icon) return;
    if (theme === 'dark') {
      icon.className = 'bi bi-moon-fill';
    } else {
      icon.className = 'bi bi-sun-fill';
    }
  }

  // Initialize all theme toggle buttons on the page
  const currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
  themeToggleButtons.forEach(button => {
    updateToggleIcon(button, currentTheme);

    button.addEventListener('click', () => {
      const activeTheme = document.documentElement.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
      
      document.documentElement.setAttribute('data-theme', activeTheme);
      localStorage.setItem('theme', activeTheme);
      
      // Update all toggle buttons on the current page
      themeToggleButtons.forEach(btn => updateToggleIcon(btn, activeTheme));
    });
  });
});
