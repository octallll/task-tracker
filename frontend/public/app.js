'use strict';

const API = '/api';

const STATUSES = ['TODO', 'IN_PROGRESS', 'DONE'];

const state = {
    boards: [],
    tags: [],
    tasks: [],
    currentBoardId: null,
    editingTaskId: null,
    editingBoardId: null,
};

const $ = (sel) => document.querySelector(sel);

async function api(method, path, body) {
    const res = await fetch(API + path, {
        method,
        headers: body ? { 'Content-Type': 'application/json' } : {},
        body: body ? JSON.stringify(body) : undefined,
    });

    if (!res.ok) {
        let message = `${res.status} ${res.statusText}`;
        try {
            const problem = await res.json();
            if (problem.detail) message = problem.detail;
        } catch (_) {}
        throw new Error(message);
    }
    return res.status === 204 ? null : res.json();
}

function showError(err) {
    const toast = $('#toast');
    toast.textContent = err.message || String(err);
    toast.hidden = false;
    clearTimeout(showError.timer);
    showError.timer = setTimeout(() => (toast.hidden = true), 4000);
}

function safe(fn) {
    return (...args) => Promise.resolve(fn(...args)).catch(showError);
}

async function loadBoards() {
    state.boards = await api('GET', '/boards');
    if (!state.boards.some((b) => b.id === state.currentBoardId)) {
        state.currentBoardId = state.boards.length ? state.boards[0].id : null;
    }
    renderBoards();
}

async function loadTags() {
    state.tags = await api('GET', '/tags');
    renderTags();
}

async function loadTasks() {
    state.tasks = state.currentBoardId ? await api('GET', `/boards/${state.currentBoardId}/tasks`) : [];
    renderTasks();
}

async function refresh() {
    await loadBoards();
    await loadTasks();
}

function el(tag, attrs = {}, ...children) {
    const node = document.createElement(tag);
    for (const [key, value] of Object.entries(attrs)) {
        if (key === 'class') node.className = value;
        else if (key.startsWith('on')) node.addEventListener(key.slice(2), value);
        else if (key === 'style') node.style.cssText = value;
        else node.setAttribute(key, value);
    }
    for (const child of children) {
        if (child != null) node.append(child);
    }
    return node;
}

function renderBoards() {
    const list = $('#board-list');
    list.replaceChildren(...state.boards.map((b) =>
        el('li', {
            class: b.id === state.currentBoardId ? 'active' : '',
            onclick: safe(async () => {
                state.currentBoardId = b.id;
                renderBoards();
                await loadTasks();
            }),
        }, el('span', {}, b.name), el('span', { class: 'count' }, String(b.taskCount)))
    ));

    const board = currentBoard();
    $('#board-head').hidden = !board;
    $('#columns').hidden = !board;
    $('#empty').hidden = !!board;
    if (board) {
        $('#board-title').textContent = board.name;
        $('#board-desc').textContent = board.description || '';
    }
}

function renderTags() {
    $('#tag-list').replaceChildren(...state.tags.map((t) =>
        el('li', {},
            el('span', { class: 'dot', style: `background:${t.color}` }),
            el('span', { class: 'name' }, t.name),
            el('button', {
                class: 'del',
                title: 'Удалить тег',
                onclick: safe(async () => {
                    if (!confirm(`Удалить тег «${t.name}»? Он пропадёт у всех задач.`)) return;
                    await api('DELETE', `/tags/${t.id}`);
                    await loadTags();
                    await loadTasks();
                }),
            }, '×'))
    ));
}

function renderTasks() {
    const today = new Date().toISOString().slice(0, 10);
    for (const column of document.querySelectorAll('.column')) {
        const status = column.dataset.status;
        const idx = STATUSES.indexOf(status);
        const cards = state.tasks.filter((t) => t.status === status).map((t) => {
            const overdue = t.dueDate && t.dueDate < today && t.status !== 'DONE';
            return el('div', { class: `card ${t.priority}`, onclick: () => openTaskDialog(t) },
                el('div', { class: 'title' }, t.title),
                el('div', { class: 'meta' },
                    t.dueDate ? el('span', { class: 'due' + (overdue ? ' overdue' : '') }, 'до ' + t.dueDate) : null,
                    ...t.tags.map((tag) => el('span', { class: 'chip', style: `background:${tag.color}` }, tag.name))),
                el('div', { class: 'move' },
                    idx > 0 ? el('button', { title: 'Назад', onclick: moveHandler(t, STATUSES[idx - 1]) }, '←') : null,
                    idx < STATUSES.length - 1 ? el('button', { title: 'Вперёд', onclick: moveHandler(t, STATUSES[idx + 1]) }, '→') : null));
        });
        column.querySelector('.cards').replaceChildren(...cards);
    }
}

function moveHandler(task, status) {
    return safe(async (event) => {
        event.stopPropagation();
        await api('PATCH', `/tasks/${task.id}/status`, { status });
        await loadTasks();
    });
}

function currentBoard() {
    return state.boards.find((b) => b.id === state.currentBoardId);
}

function openBoardDialog(board) {
    state.editingBoardId = board ? board.id : null;
    const form = $('#board-form');
    form.reset();
    $('#board-dialog-title').textContent = board ? 'Изменить доску' : 'Новая доска';
    form.name.value = board ? board.name : '';
    form.description.value = board ? board.description || '' : '';
    const dialog = $('#board-dialog');
    dialog.returnValue = '';
    dialog.showModal();
}

$('#board-dialog').addEventListener('close', safe(async () => {
    const dialog = $('#board-dialog');
    if (dialog.returnValue !== 'save') return;
    const form = $('#board-form');
    const body = { name: form.name.value, description: form.description.value || null };
    if (state.editingBoardId) {
        await api('PUT', `/boards/${state.editingBoardId}`, body);
    } else {
        const created = await api('POST', '/boards', body);
        state.currentBoardId = created.id;
    }
    await refresh();
}));

function openTaskDialog(task) {
    state.editingTaskId = task ? task.id : null;
    const form = $('#task-form');
    form.reset();
    $('#task-dialog-title').textContent = task ? 'Изменить задачу' : 'Новая задача';
    $('#delete-task').hidden = !task;
    form.title.value = task ? task.title : '';
    form.description.value = task ? task.description || '' : '';
    form.status.value = task ? task.status : 'TODO';
    form.priority.value = task ? task.priority : 'MEDIUM';
    form.dueDate.value = task && task.dueDate ? task.dueDate : '';

    const selected = new Set(task ? task.tags.map((t) => t.id) : []);
    $('#tag-picker').replaceChildren(...(state.tags.length ? state.tags.map((t) => {
        const box = el('input', { type: 'checkbox', value: String(t.id) });
        box.checked = selected.has(t.id);
        return el('label', {}, box, el('span', { class: 'dot', style: `background:${t.color}` }), t.name);
    }) : [el('span', { class: 'muted' }, 'Тегов пока нет — создайте их слева')]));

    const dialog = $('#task-dialog');
    dialog.returnValue = '';
    dialog.showModal();
}

$('#task-dialog').addEventListener('close', safe(async () => {
    const action = $('#task-dialog').returnValue;
    const form = $('#task-form');
    if (action === 'delete' && state.editingTaskId) {
        if (!confirm('Удалить задачу?')) return;
        await api('DELETE', `/tasks/${state.editingTaskId}`);
    } else if (action === 'save') {
        const body = {
            title: form.title.value,
            description: form.description.value || null,
            status: form.status.value,
            priority: form.priority.value,
            dueDate: form.dueDate.value || null,
            tagIds: [...document.querySelectorAll('#tag-picker input:checked')].map((i) => Number(i.value)),
        };
        if (state.editingTaskId) {
            await api('PUT', `/tasks/${state.editingTaskId}`, body);
        } else {
            await api('POST', `/boards/${state.currentBoardId}/tasks`, body);
        }
    } else {
        return;
    }
    await refresh();
}));

$('#add-board').addEventListener('click', () => openBoardDialog(null));
$('#edit-board').addEventListener('click', () => openBoardDialog(currentBoard()));
$('#add-task').addEventListener('click', () => openTaskDialog(null));

$('#delete-board').addEventListener('click', safe(async () => {
    const board = currentBoard();
    if (!board || !confirm(`Удалить доску «${board.name}» вместе со всеми задачами?`)) return;
    await api('DELETE', `/boards/${board.id}`);
    state.currentBoardId = null;
    await refresh();
}));

$('#tag-form').addEventListener('submit', safe(async (event) => {
    event.preventDefault();
    const form = event.target;
    await api('POST', '/tags', { name: form.name.value, color: form.color.value });
    form.reset();
    await loadTags();
}));

safe(async () => {
    await Promise.all([loadBoards(), loadTags()]);
    await loadTasks();
})();
