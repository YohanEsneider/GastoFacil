const fs = require('fs');
const path = require('path');

const directory = __dirname;
const files = fs.readdirSync(directory).filter(file => file.endsWith('.html'));

files.forEach(file => {
  const filePath = path.join(directory, file);
  let content = fs.readFileSync(filePath, 'utf-8');
  
  // Replace the <style> tag and its contents with the link to styles.css
  content = content.replace(/<style>[\s\S]*?<\/style>/, '<link rel="stylesheet" href="styles.css">');
  
  // Minor HTML tweaks specific to new premium UI, e.g. adding glass-morphism class if necessary,
  // but just adding the stylesheet is enough since classes are generic.
  
  fs.writeFileSync(filePath, content, 'utf-8');
  console.log(`Updated ${file}`);
});
