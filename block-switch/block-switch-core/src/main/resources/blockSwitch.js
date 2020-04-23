function addBlockSwitches() {
	$('.primary').each(function() {
		primary = $(this);
		createSwitchItem(primary, createBlockSwitch(primary)).item.addClass("selected");
		primary.children('.title').remove();
	});
	$('.secondary').each(function(idx, node) {
		secondary = $(node);
		primary = findPrimary(secondary);
		switchItem = createSwitchItem(secondary, primary.children('.switch'));
		switchItem.content.addClass('hidden');
		findPrimary(secondary).append(switchItem.content);
		secondary.remove();
	});
}

function createBlockSwitch(primary) {
	blockSwitch = $('<div class="switch"></div>');
	primary.prepend(blockSwitch);
	return blockSwitch;
}

function findPrimary(secondary) {
	candidate = secondary.prev();
	while (!candidate.is('.primary')) {
		candidate = candidate.prev();
	}
	return candidate;
}

function createSwitchItem(block, blockSwitch) {
	blockName = block.children('.title').text();
	category = blockName;
	$(block).get(0).classList.forEach(function(value) {
		if (value.startsWith('category-')) {
			category = value.substring(9);
		}
	})
	item = $('<div class="switch--item">' + blockName + '</div>');
	item.attr('category', category);
	blockSwitch.append(item);	
	content = block.children('.content').first().append(block.next('.colist'));
	return {'item': item, 'content': content};
}

function globalSwitch() {
	$('.switch--item').each(function() {
		var blockId = blockIdForSwitchItem($(this));
		$(this).off('click');
		$(this).on('click', function() {
			selectedCategory = $(this).attr('category');
			window.localStorage.setItem(blockId, selectedCategory);
			$(".switch--item").filter(function() {
				return blockIdForSwitchItem($(this)) === blockId;
			}).filter(function() {
				return $(this).attr('category') === selectedCategory;
			}).each(function() {
				select($(this))
			});
		});
		if ($(this).attr('category') === window.localStorage.getItem(blockId)) {
			select($(this))
		}
	});
}

function blockIdForSwitchItem(item) {
	idComponents = []
	idComponents.push(item.attr('category').toLowerCase());
	item.siblings(".switch--item").each(function(index, sibling) {
		idComponents.push($(sibling).attr('category').toLowerCase());
	});
	return idComponents.sort().join("-")
}

function select(selected) {
	selected.addClass('selected');
	selected.siblings().removeClass('selected');
	selectedContent = selected.parent().siblings(".content").eq(selected.index())
	selectedContent.removeClass('hidden');
	selectedContent.siblings().addClass('hidden');
}

$(addBlockSwitches);
$(globalSwitch);
