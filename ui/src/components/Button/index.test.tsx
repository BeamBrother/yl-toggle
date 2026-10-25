import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom/vitest';
import userEvent from '@testing-library/user-event';
import Button from '.';

it('button snapshot', async () => {
  await (async () => {
    const { asFragment } = render(<Button>this is a Button</Button>);
    expect(asFragment()).toMatchSnapshot();
  })();
});

it('click button', async () => {
  await (async () => {
    const { rerender } = render(<Button key={1} onClick={(e) => {e.currentTarget.innerHTML = 'you clicked the button';}}>this is a Button</Button>);
    let html = screen.getByRole('button');
    await userEvent.click(html);
    expect(html.innerHTML).toBe('you clicked the button');

    rerender(<Button key={2}>this is a Button</Button>);
    html = screen.getByRole('button');
    await userEvent.click(html);
    expect(html.innerHTML).toBe('this is a Button');
  })();
});
