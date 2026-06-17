import Splide from '@splidejs/splide';

window.initMangaSplide = function (carouselEl, dir, initialPageIndex,
    pageCount) {
  // Clean up any old splide instance on this element
  if (carouselEl._splide) {
    try {
      carouselEl._splide.destroy();
    } catch (e) {
      console.error("Error destroying old splide", e);
    }
  }

  const splide = new Splide(carouselEl, {
    type: 'slide',
    rewind: false,
    speed: 250, // Slightly faster transition speed for a snappier feel
    perPage: 1,
    arrows: false,
    pagination: false,
    direction: dir, // 'ltr' or 'rtl'
    drag: true,
    keyboard: false, // Managed by server/shortcuts to avoid input focus conflicts
    lazyLoad: 'nearby',
    preloadPages: 2, // Preload 2 pages ahead/behind to prevent loading delays during flipping
    start: initialPageIndex || 0,
  });

  splide.mount();
  carouselEl._splide = splide;

  // Handle page views immediately when the slide transition starts
  splide.on('move', (newIndex) => {
    // Send event to server
    const event = new CustomEvent('manga-page-view', {
      detail: {pageIndex: newIndex},
      bubbles: true
    });
    carouselEl.dispatchEvent(event);

    // Update UI controls immediately on the client side for instant feedback
    const readerContainer = carouselEl.closest('.manga-reader');
    if (readerContainer) {
      const controls = readerContainer.querySelector('.controls');
      if (controls) {
        // Update the progress bar CSS variable
        if (pageCount && pageCount > 0) {
          const progress = ((newIndex + 1) / pageCount) * 100;
          controls.style.setProperty('--reader-progress', progress + '%');
        }
        // Update the text field input value
        const input = controls.querySelector('.page-input');
        if (input) {
          input.value = String(newIndex + 1);
        }
      }
    }
  });
};
